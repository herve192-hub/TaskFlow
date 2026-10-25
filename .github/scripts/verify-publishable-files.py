#!/usr/bin/env python3
# Publication boundary used by hooks and security CI. Validate staged or committed
# Git blobs, rejecting private/generated paths, symlinks, oversized files, and tracked
# ignored files. Optional working-tree mode reviews pending unignored edits; ignored
# runtime secrets are never copied into the scanner input. Secret checks require an
# exact pinned Gitleaks version and can include all locally available history.
# This audits selected content; it neither stages files nor sends anything to GitHub.

"""Check Git's actual publishable files without reading ignored runtime secrets."""

import argparse
import os
from pathlib import Path, PurePosixPath
import shutil
import subprocess
import tempfile


FORBIDDEN_DIRECTORIES = {
    "node_modules", "target", "build", "out", "dist", "dist-ssr", "coverage",
    "test-results", "playwright-report", "blob-report", "__pycache__", "logs",
    "secrets", ".secrets", "credentials", "data", "backups", "codeql-results",
    ".aws", ".azure", ".kube", ".ssh", ".gnupg", ".codex", ".agents",
    ".idea", ".vscode", ".history", ".terraform",
}
FORBIDDEN_SUFFIXES = {
    ".pem", ".key", ".private-key", ".password", ".p12", ".pfx", ".jks", ".keystore", ".db", ".sqlite",
    ".sqlite3", ".dump", ".sql", ".bson", ".rdb", ".aof", ".bak", ".backup",
    ".jar", ".war", ".class", ".pyc", ".pyo", ".log", ".sarif", ".zip",
    ".tar", ".tgz", ".tmp", ".swp", ".swo", ".tsbuildinfo", ".har", ".hprof", ".heapdump",
}
GITLEAKS_VERSION = "8.30.1"
MAX_FILE_BYTES = 10 * 1024 * 1024


# Use argument arrays and binary output so Git paths/blobs remain lossless;
# NUL-delimited callers handle whitespace/newlines in filenames.
def git(*args, input_data=None):
    return subprocess.check_output(["git", *args], input=input_data)


# Resolve explicit override, configured/managed local install, or PATH, then check
# the exact required version. Never silently skip a missing scanner in publication gates.
def gitleaks_binary():
    repair = "Run: python3 .github/scripts/install-gitleaks.py"
    configured = subprocess.run(
        ["git", "config", "--local", "--get", "taskflow.gitleaksPath"],
        capture_output=True, text=True, check=False,
    ).stdout.strip()
    explicit = os.environ.get("GITLEAKS_BIN")
    common_dir = Path(os.fsdecode(git("rev-parse", "--path-format=absolute", "--git-common-dir")).strip())
    managed = common_dir / "taskflow-tools" / "gitleaks" / GITLEAKS_VERSION / "gitleaks"
    candidates = [explicit] if explicit else [configured, str(managed), "gitleaks"]
    binary = next((resolved for candidate in candidates if candidate and (resolved := shutil.which(candidate))), None)
    if not binary:
        detail = "GITLEAKS_BIN is not executable." if explicit else "No executable scanner was found; configured paths may be stale."
        raise SystemExit(f"Gitleaks {GITLEAKS_VERSION} is unavailable. {detail}\n{repair}")
    try:
        version = subprocess.check_output([binary, "version"], text=True, stderr=subprocess.DEVNULL).strip()
    except (OSError, subprocess.CalledProcessError):
        raise SystemExit(f"Cannot run the selected Gitleaks scanner.\n{repair}") from None
    if version.removeprefix("v") != GITLEAKS_VERSION:
        raise SystemExit(f"Publication checks require Gitleaks {GITLEAKS_VERSION}; the selected scanner has a different version.\n{repair}")
    return binary


# Choose committed/index blobs by default, or explicit working-tree review mode.
# Validate paths/modes before reading contents, then stage only approved content in
# a disposable scanner directory; history scans stay local and use redacted output.
def check(args):
    root = Path(os.fsdecode(git("rev-parse", "--show-toplevel")).strip())
    os.chdir(root)
    files = {}
    if args.ref:
        args.ref = git("rev-parse", "--verify", "--end-of-options", args.ref + "^{commit}").decode().strip()
        for record in git("ls-tree", "-rz", args.ref).split(b"\0"):
            if record:
                metadata, name = record.split(b"\t", 1)
                mode, _, oid = metadata.decode().split()
                files[os.fsdecode(name)] = (mode, oid)
    else:
        for record in git("ls-files", "--stage", "-z").split(b"\0"):
            if record:
                metadata, name = record.split(b"\t", 1)
                mode, oid, stage = metadata.decode().split()
                if stage != "0":
                    raise SystemExit("Resolve Git conflicts before publication checks.")
                files[os.fsdecode(name)] = (mode, oid)
    if args.include_untracked:
        # Audit pending working-tree edits, while normal hooks check index blobs.
        files = {
            name: ("120000" if Path(name).is_symlink() else mode, None)
            for name, (mode, _) in files.items()
            if Path(name).exists() or Path(name).is_symlink()
        }
        for name in git("ls-files", "--others", "--exclude-standard", "-z").split(b"\0"):
            if name:
                path = os.fsdecode(name)
                files[path] = ("120000" if Path(path).is_symlink() else "100644", None)

    ignored = subprocess.run(
        ["git", "check-ignore", "--no-index", "--stdin", "-z"],
        input=b"".join(os.fsencode(name) + b"\0" for name in files),
        capture_output=True, check=False,
    )
    if ignored.returncode not in (0, 1):
        raise SystemExit("Cannot evaluate Git ignore rules.")
    ignored_names = {os.fsdecode(name) for name in ignored.stdout.split(b"\0") if name}
    failures = []
    for name, (mode, oid) in sorted(files.items()):
        path = PurePosixPath(name)
        base = path.name.lower()
        reason = None
        if path.is_absolute() or ".." in path.parts or mode not in {"100644", "100755"}:
            reason = "unsafe path, symlink, or submodule"
        elif name in ignored_names:
            reason = "ignored file is already tracked"
        elif FORBIDDEN_DIRECTORIES.intersection(part.lower() for part in path.parts[:-1]):
            reason = "private or generated directory"
        elif base.startswith(".env") or ".env." in base or base.endswith(".env"):
            reason = "environment configuration"
        elif ".tfstate" in base or base.endswith((".tfvars", ".tfvars.json", ".tfplan")) or ".tfplan." in base:
            reason = "Terraform state, deployment inputs, or saved plan output"
        elif path.suffix.lower() in FORBIDDEN_SUFFIXES or base.endswith((".tar.gz", "~")):
            reason = "key material, local data, or generated output"
        elif base in {".netrc", ".git-credentials", ".credentials", ".htpasswd", "client.properties"} or base.startswith(("id_rsa", "id_ed25519")):
            reason = "credential file"
        elif (base.startswith(("security-", "trivy")) and base.endswith(".json")) or (base.startswith("gitleaks") and base.endswith("-report.json")):
            reason = "raw security report"
        else:
            size = int(git("cat-file", "-s", oid)) if oid else Path(name).stat().st_size
            if size > MAX_FILE_BYTES:
                reason = "file exceeds the 10 MiB publication limit"
        if reason:
            failures.append((name, reason))
    if failures:
        for name, reason in failures:
            print(f"Blocked: {name!r}: {reason}")
        raise SystemExit("Remove these files from Git's index; keep private files outside source directories.")
    print(f"Publication file policy passed for {len(files)} files.")

    if args.secrets or args.history:
        binary = gitleaks_binary()
        if args.secrets:
            # Scan the indexed/committed blobs, including files that were edited again
            # after staging. Never copy ignored secrets or follow external symlinks.
            with tempfile.TemporaryDirectory(prefix="taskflow-publish-") as directory:
                for name, (_, oid) in files.items():
                    content = git("cat-file", "blob", oid) if oid else Path(name).read_bytes()
                    destination = Path(directory) / name
                    destination.parent.mkdir(parents=True, exist_ok=True)
                    destination.write_bytes(content)
                subprocess.run(
                    [binary, "dir", "--redact", "--no-banner", "--ignore-gitleaks-allow", directory],
                    check=True,
                )
        if args.history:
            subprocess.run(
                [binary, "git", "--redact", "--no-banner", "--ignore-gitleaks-allow", "--log-opts=--all" + (" " + args.ref if args.ref else "")],
                check=True,
            )


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--include-untracked", action="store_true", help="also review unignored new files")
    parser.add_argument("--ref", help="review a committed tree instead of the index")
    parser.add_argument("--secrets", action="store_true", help="scan the selected file contents with pinned Gitleaks")
    parser.add_argument("--history", action="store_true", help="also scan all locally available Git history")
    options = parser.parse_args()
    if options.ref and options.include_untracked:
        parser.error("--ref and --include-untracked cannot be combined")
    try:
        check(options)
    except (OSError, subprocess.CalledProcessError) as error:
        raise SystemExit(f"Publication check failed ({type(error).__name__}); nothing was published.") from None
