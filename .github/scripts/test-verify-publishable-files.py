#!/usr/bin/env python3
# Publication/installer regression tests in disposable Git repositories and archives.
# Exercise staged versus unstaged content, forbidden paths, scanner discovery/version,
# secret/history invocation, and checksum/archive validation without publishing.
# Fixtures are synthetic and temporary; the real repository index remains untouched.

"""Exercise publication protections against real disposable Git repositories."""

import hashlib
import io
import os
from pathlib import Path
import runpy
import shutil
import subprocess
import sys
import tarfile
import tempfile
import unittest


SCRIPT = Path(__file__).with_name("verify-publishable-files.py").resolve()


class PublicationChecksTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory(prefix="taskflow-publish-test-")
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        self.git("init", "--quiet")
        self.git("config", "user.name", "Publication Test")
        self.git("config", "user.email", "ci@example.invalid")
        self.write(".gitignore", ".env*\nsecrets/\nnode_modules/\n")
        self.git("add", ".gitignore")

    def git(self, *args):
        return subprocess.run(
            ["git", "-c", "core.hooksPath=/dev/null", *args], cwd=self.root,
            capture_output=True, text=True, check=True,
        )

    def write(self, name, content):
        destination = self.root / name
        destination.parent.mkdir(parents=True, exist_ok=True)
        destination.write_text(content)

    def check(self, *args, env=None):
        return subprocess.run(
            [sys.executable, str(SCRIPT), *args], cwd=self.root,
            capture_output=True, text=True, check=False, env=env,
        )

    def isolated_scanner_environment(self):
        environment = os.environ.copy()
        environment.pop("GITLEAKS_BIN", None)
        executables = self.root / "executables"
        executables.mkdir()
        (executables / "git").symlink_to(shutil.which("git"))
        environment["PATH"] = str(executables)
        return environment

    def managed_scanner(self):
        scanner = os.environ.get("GITLEAKS_BIN") or shutil.which("gitleaks")
        self.assertTrue(scanner, "Set GITLEAKS_BIN to the installed scanner when running this test suite")
        destination = self.root / ".git/taskflow-tools/gitleaks/8.30.1/gitleaks"
        destination.parent.mkdir(parents=True)
        destination.symlink_to(Path(scanner).resolve())
        return destination

    def synthetic_secret(self):
        # Assemble a scanner fixture at runtime; no literal credential is committed.
        return "ghp_" + hashlib.sha256(b"disposable-publication-test").hexdigest()[:36]

    def test_required_source_lockfiles_and_public_template_are_allowed(self):
        for name in ("frontend/src/App.tsx", "frontend/package-lock.json", "deploy/production/environment.example",
                     "deploy/aws/terraform/providers.tf", "deploy/aws/terraform/.terraform.lock.hcl",
                     "deploy/aws/terraform/settings.example"):
            self.write(name, "Reviewed public build input\n")
            self.git("add", name)
        self.assertEqual(self.check().returncode, 0)

    def test_no_environment_files_can_be_force_added(self):
        for name in (".env", "frontend/.env.production", "deploy/.env.example", "config/service.env", "config/service.env.example"):
            with self.subTest(name=name):
                self.write(name, "PUBLIC_SETTING=example\n")
                self.git("add", "--force", name)
                self.assertNotEqual(self.check().returncode, 0)
                self.git("rm", "--cached", name)

    def test_ignored_generated_files_are_blocked_even_when_force_added(self):
        self.write("node_modules/.package-lock.json", "{}\n")
        self.git("add", "--force", "node_modules/.package-lock.json")
        self.assertNotEqual(self.check().returncode, 0)

    def test_terraform_runtime_files_are_blocked_without_relying_on_ignore_rules(self):
        # A forced addition or a file saved outside deploy/aws/terraform must not
        # publish state, account inputs, plans, or downloaded backend metadata.
        for name in ("terraform.tfstate", "terraform.tfstate.backup", "terraform.tfstate.1234",
                     "config/production.tfvars", "config/production.auto.tfvars.json",
                     "releases/production.tfplan", "releases/production.tfplan.json",
                     "cache/.terraform/backend.json"):
            with self.subTest(name=name):
                self.write(name, "{}\n")
                self.git("add", "--force", name)
                self.assertNotEqual(self.check().returncode, 0)
                self.git("rm", "--cached", name)

    def test_symlinks_cannot_leak_external_files(self):
        (self.root / "external.txt").symlink_to("/etc/passwd")
        self.git("add", "external.txt")
        self.assertNotEqual(self.check().returncode, 0)

    def test_secret_mount_files_are_blocked_outside_the_secrets_directory(self):
        for name in ("config/jwt.private-key", "config/spring.security.user.password", "config/client.properties"):
            with self.subTest(name=name):
                self.write(name, "disposable-fixture\n")
                self.git("add", name)
                self.assertNotEqual(self.check().returncode, 0)
                self.git("rm", "--cached", name)

    def test_staged_secret_is_detected_after_working_file_is_cleaned(self):
        token = self.synthetic_secret()
        self.write("settings.txt", "api_token=" + token + "\n")
        self.git("add", "settings.txt")
        self.write("settings.txt", "api_token=removed\n")
        result = self.check("--secrets")
        self.assertNotEqual(result.returncode, 0)
        self.assertTrue(token not in result.stdout + result.stderr, "Scanner output must redact credentials")

    def test_pending_edits_are_scanned_without_reading_ignored_secrets(self):
        self.write("settings.txt", "api_token=public-placeholder\n")
        self.git("add", "settings.txt")
        token = self.synthetic_secret()
        self.write("secrets/private.txt", token)
        self.write(".env", "api_token=" + token)
        self.assertEqual(self.check("--include-untracked", "--secrets").returncode, 0)
        self.write("settings.txt", "api_token=" + token)
        self.assertNotEqual(self.check("--include-untracked", "--secrets").returncode, 0)

    def test_removed_secret_is_still_detected_in_history(self):
        self.write("settings.txt", "api_token=" + self.synthetic_secret())
        self.git("add", "settings.txt")
        self.git("commit", "--quiet", "-m", "Disposable secret fixture")
        self.write("settings.txt", "api_token=removed\n")
        self.git("add", "settings.txt")
        self.git("commit", "--quiet", "-m", "Remove disposable fixture")
        self.assertEqual(self.check("--ref", "HEAD", "--secrets").returncode, 0)
        self.assertNotEqual(self.check("--ref", "HEAD", "--history").returncode, 0)

    def test_missing_scanner_reports_the_repair_command(self):
        self.git("config", "taskflow.gitleaksPath", str(self.root / "removed-scanner"))
        result = self.check("--secrets", env=self.isolated_scanner_environment())
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("python3 .github/scripts/install-gitleaks.py", result.stderr)
        self.assertNotIn("FileNotFoundError", result.stderr)

    def test_stale_configuration_falls_back_to_the_persistent_scanner(self):
        self.git("config", "taskflow.gitleaksPath", str(self.root / "removed-scanner"))
        self.managed_scanner()
        result = self.check("--secrets", env=self.isolated_scanner_environment())
        self.assertEqual(result.returncode, 0, result.stderr)

    def test_explicit_missing_scanner_cannot_silently_use_a_different_binary(self):
        self.managed_scanner()
        environment = self.isolated_scanner_environment()
        environment["GITLEAKS_BIN"] = str(self.root / "missing-explicit-scanner")
        result = self.check("--secrets", env=environment)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("GITLEAKS_BIN is not executable", result.stderr)

    def test_wrong_scanner_version_is_rejected(self):
        scanner = self.root / "old-scanner"
        scanner.write_text("#!/bin/sh\nprintf '8.0.0\\n'\n")
        scanner.chmod(0o755)
        self.git("config", "taskflow.gitleaksPath", str(scanner))
        result = self.check("--secrets", env=self.isolated_scanner_environment())
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("require Gitleaks 8.30.1", result.stderr)


class ScannerInstallationTest(unittest.TestCase):
    def setUp(self):
        self.verify = runpy.run_path(str(SCRIPT.with_name("install-gitleaks.py")))["verified_binary"]
        stream = io.BytesIO()
        with tarfile.open(fileobj=stream, mode="w:gz") as archive:
            member = tarfile.TarInfo("gitleaks")
            member.size = len(b"fixture executable")
            archive.addfile(member, io.BytesIO(b"fixture executable"))
        self.archive = stream.getvalue()
        self.name = "gitleaks_fixture.tar.gz"

    def test_only_checksum_verified_executable_is_returned(self):
        checksum = hashlib.sha256(self.archive).hexdigest()
        self.assertEqual(self.verify(self.archive, checksum + "  " + self.name, self.name), b"fixture executable")

    def test_corrupt_download_is_rejected_before_extraction(self):
        with self.assertRaisesRegex(SystemExit, "checksum verification failed"):
            self.verify(self.archive, "0" * 64 + "  " + self.name, self.name)


if __name__ == "__main__":
    unittest.main()
