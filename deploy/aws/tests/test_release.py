# Offline regression coverage for the release/deployment trust boundaries.
# Synthetic Terraform settings/manifests, mocked AWS/Docker commands, and temporary
# files exercise validation, archive allowlisting, property escaping, and secret-file
# permissions. No real AWS credentials, image publication, or host deployment occurs.

"""Exercise release boundaries without AWS credentials or a Docker daemon."""

import importlib.util
import json
from pathlib import Path
import stat
import tarfile
import tempfile
from types import SimpleNamespace
import unittest
from unittest.mock import patch

spec = importlib.util.spec_from_file_location("taskflow_aws", Path(__file__).resolve().parents[1] / "taskflow.py")
release = importlib.util.module_from_spec(spec)
spec.loader.exec_module(release)


def settings():
    return {
        "region": "us-east-1", "origin": "https://taskflow.example.test",
        "application_secret_arn": "arn:aws:secretsmanager:us-east-1:123456789012:secret:application-fixture",
        "bootstrap_secret_arn": "arn:aws:secretsmanager:us-east-1:123456789012:secret:bootstrap-fixture",
        "log_group": "/taskflow/test", "alb_subnet_cidrs": ["10.1.0.0/24", "10.1.1.0/24"],
        "release_bucket": "taskflow-test-releases",
        "ecr_repositories": {name: f"123456789012.dkr.ecr.us-east-1.amazonaws.com/taskflow/{name}" for name in release.DOCKERFILES},
    }


def manifest():
    config = settings()
    return {"version": 1, "release": "v1.2.3", "deployment": config,
            "images": {name: url + "@sha256:" + "a" * 64 for name, url in config["ecr_repositories"].items()}}


def secret_values():
    values = dict.fromkeys(release.APPLICATION_KEYS, "synthetic-fixture-value")
    values["spring.security.user.name"] = "infrastructure"
    values["spring.security.user.password"] = "synthetic-fixture-with-at-least-32-characters"
    return values


class ReleaseTests(unittest.TestCase):
    def test_manifest_rejects_mutable_missing_and_foreign_images(self):
        release.manifest_valid(manifest())
        for image in ["taskflow/frontend:latest", settings()["ecr_repositories"]["frontend"] + ":v1.2.3",
                      "999999999999.dkr.ecr.us-east-1.amazonaws.com/taskflow/frontend@sha256:" + "a" * 64]:
            value = manifest()
            value["images"]["frontend"] = image
            with self.assertRaises(ValueError):
                release.manifest_valid(value)
        value = manifest()
        del value["images"]["auth-service"]
        with self.assertRaises(ValueError):
            release.manifest_valid(value)

    def test_settings_reject_insecure_origins_and_broad_proxy_trust(self):
        for update in [{"origin": "http://taskflow.example.test"}, {"origin": "https://taskflow.example.test/"},
                       {"alb_subnet_cidrs": ["0.0.0.0/0", "10.1.0.0/24"]}]:
            with self.assertRaises(ValueError):
                release.settings_valid({**settings(), **update})

    def test_secret_schema_rejects_bootstrap_and_paths_in_routine_secret(self):
        for extra in [{"taskflow.bootstrap.admin.password": "fixture"}, {"../../outside": "fixture"}]:
            response = {"SecretString": json.dumps({**secret_values(), **extra})}
            with patch.object(release, "aws", return_value=json.dumps(response)):
                with self.assertRaises(ValueError):
                    release.read_secret("us-east-1", "fixture", release.APPLICATION_KEYS)

    def test_secret_files_have_scoped_names_owners_and_modes(self):
        with tempfile.TemporaryDirectory() as temporary, patch.object(release.os, "chown") as chown:
            directory = Path(temporary)
            values = secret_values()
            values["spring.security.user.password"] = "synthetic-password-32-characters:=\\ with spaces"
            release.write_secrets(directory, values, settings())
            self.assertEqual(stat.S_IMODE((directory / "jwt.private-key").stat().st_mode), 0o400)
            for call in chown.call_args_list:
                self.assertEqual(call.args[1:], (10001, 10001))
            properties = (directory / "client.properties").read_text()
            self.assertIn("spring.config.import=configserver:http://config-server:8888\n", properties)
            self.assertIn("\\:\\=\\\\\\ with\\ spaces", properties)
            self.assertEqual((directory / "eureka.client.service-url.defaultZone").read_text(), "http://discovery-server:8761/eureka/")
            self.assertEqual((directory / "alb-trust.conf").read_text(), "set_real_ip_from 10.1.0.0/24;\nset_real_ip_from 10.1.1.0/24;\n")
            self.assertFalse((directory / "taskflow.bootstrap.admin.password").exists())

    def test_publisher_excludes_private_files_and_disables_public_registration(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            for name in release.RELEASE_FILES + ["backend/config-repo/application.yml", "frontend/.env", "secrets/local.password"]:
                path = root / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text("fixture")
            config = root / "settings.json"
            config.write_text(json.dumps(settings()))
            commands = []

            def fake_run(command, **kwargs):
                commands.append(command)
                return b"" if command[1] == "status" else b"a" * 40

            def fake_aws(region, *args):
                if args[:2] == ("ecr", "describe-images"):
                    return json.dumps({"imageDetails": [{"imageDigest": "sha256:" + "a" * 64}]})
                if args[:2] == ("s3", "cp"):
                    with tarfile.open(args[2]) as bundle:
                        self.assertEqual(set(bundle.getnames()), set(release.RELEASE_FILES + ["backend/config-repo/application.yml", "release.json"]))
                        release.manifest_valid(json.load(bundle.extractfile("release.json")))
                    return b""
                self.fail("Unexpected AWS operation")

            with patch.object(release, "ROOT", root), patch.object(release, "run", side_effect=fake_run), \
                    patch.object(release, "aws", side_effect=fake_aws), patch.object(release, "docker_login") as login:
                login.return_value.__enter__.return_value = {}
                release.publish(SimpleNamespace(settings=str(config), release="v1.2.3"))
            builds = [command for command in commands if command[:2] == ["docker", "build"]]
            self.assertEqual(len(builds), 8)
            self.assertTrue(all(command[2:4] == ["--platform", "linux/amd64"] for command in builds))
            self.assertIn("VITE_PUBLIC_REGISTRATION_ENABLED=false", builds[0])

    def test_bootstrap_recreates_auth_without_bootstrap_mounts_and_erases_host_copies(self):
        with tempfile.TemporaryDirectory() as temporary:
            host = Path(temporary) / "host"
            root = Path(temporary) / "release"
            (host / "secrets").mkdir(parents=True)
            (host / "host-ready").touch()
            root.mkdir()
            (root / "release.json").write_text(json.dumps(manifest()))
            bootstrap = {name: "synthetic-bootstrap-fixture" for name in release.BOOTSTRAP_KEYS}
            with patch.object(release, "ROOT", root), patch.object(release, "HOST", host), \
                    patch.object(release.os, "geteuid", return_value=0), patch.object(release.os, "chown"), \
                    patch.object(release, "read_secret", side_effect=[secret_values(), bootstrap]), \
                    patch.object(release, "docker_login") as login, patch.object(release, "run") as run:
                login.return_value.__enter__.return_value = {}
                release.deploy(SimpleNamespace(bootstrap=True))
            self.assertEqual((host / "current").resolve(), root)
            invocations = [call.args[0] for call in run.call_args_list]
            self.assertIn(str(root / "deploy/production/compose.bootstrap.yml"), invocations[-2])
            self.assertNotIn(str(root / "deploy/production/compose.bootstrap.yml"), invocations[-1])
            self.assertEqual(invocations[-1][-1], "auth-service")
            self.assertEqual(len(list((host / "secrets").glob("*/jwt.private-key"))), 1)
            self.assertFalse(list((host / "secrets").glob("*/taskflow.bootstrap.admin.*")))


if __name__ == "__main__":
    unittest.main()
