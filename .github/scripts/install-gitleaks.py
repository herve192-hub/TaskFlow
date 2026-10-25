#!/usr/bin/env python3
# Developer scanner installer for publication hooks. Reads the required version
# from verify-publishable-files.py, downloads the platform release and checksum list,
# verifies bytes/version, then atomically installs in Git's shared local metadata.
# Only the executable member is read; archive paths are never extracted. Git local
# configuration records the path so worktrees reuse it. Installation needs network
# and Git-metadata write access but does not modify tracked source or enable hooks.

"""Install the pinned, checksum-verified scanner in Git's persistent local metadata."""

import hashlib
import io
import os
from pathlib import Path
import platform
import runpy
import subprocess
import tarfile
import tempfile
from urllib.error import URLError
from urllib.request import urlopen


VERSION = runpy.run_path(str(Path(__file__).with_name("verify-publishable-files.py")))["GITLEAKS_VERSION"]


# Validate the chosen archive checksum and a bounded regular executable member.
# Return bytes for a later atomic install without extracting arbitrary archive paths.
def verified_binary(archive, checksums, archive_name):
    expected = [
        fields[0] for line in checksums.splitlines()
        if len(fields := line.split()) == 2 and fields[1].lstrip("*") == archive_name
    ]
    if len(expected) != 1 or hashlib.sha256(archive).hexdigest() != expected[0]:
        raise SystemExit("Gitleaks checksum verification failed; nothing was installed.")
    # Read only the executable, never extract archive paths into the filesystem.
    with tarfile.open(fileobj=io.BytesIO(archive), mode="r:gz") as bundle:
        member = bundle.getmember("gitleaks")
        if not member.isfile() or member.size > 128 * 1024 * 1024:
            raise SystemExit("Invalid Gitleaks executable in release archive.")
        return bundle.extractfile(member).read()


# Resolve platform/version, download and verify upstream release bytes, run its
# version check, then atomically replace the local binary and record its Git path.
def install():
    system = {"Linux": "linux", "Darwin": "darwin"}.get(platform.system())
    machine = {"x86_64": "x64", "amd64": "x64", "aarch64": "arm64", "arm64": "arm64"}.get(platform.machine().lower())
    if not system or not machine:
        raise SystemExit("Install Gitleaks " + VERSION + " manually on this platform; see README.md.")
    common_dir = Path(subprocess.check_output(
        ["git", "rev-parse", "--path-format=absolute", "--git-common-dir"], text=True,
    ).strip())
    archive_name = f"gitleaks_{VERSION}_{system}_{machine}.tar.gz"
    base = f"https://github.com/gitleaks/gitleaks/releases/download/v{VERSION}"
    print(f"Downloading and verifying Gitleaks {VERSION} for {system}/{machine}...", flush=True)
    with urlopen(f"{base}/{archive_name}", timeout=60) as response:
        archive = response.read()
    with urlopen(f"{base}/gitleaks_{VERSION}_checksums.txt", timeout=60) as response:
        checksums = response.read().decode("utf-8")
    binary = verified_binary(archive, checksums, archive_name)
    destination = common_dir / "taskflow-tools" / "gitleaks" / VERSION / "gitleaks"
    destination.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="install-", dir=destination.parent) as directory:
        candidate = Path(directory) / "gitleaks"
        candidate.write_bytes(binary)
        candidate.chmod(0o755)
        installed_version = subprocess.check_output([str(candidate), "version"], text=True).strip()
        if installed_version.removeprefix("v") != VERSION:
            raise SystemExit("Downloaded scanner has an unexpected version; nothing was installed.")
        os.replace(candidate, destination)
    subprocess.run(["git", "config", "--local", "taskflow.gitleaksPath", str(destination)], check=True)
    print(f"Installed checksum-verified Gitleaks {VERSION} at {destination}")
    print("The scanner stays local to Git metadata and is never committed or pushed.")


if __name__ == "__main__":
    try:
        install()
    except (OSError, URLError, subprocess.CalledProcessError, tarfile.TarError, KeyError) as error:
        raise SystemExit(f"Gitleaks installation failed ({type(error).__name__}). Check network access and Git directory permissions, then retry.") from None
