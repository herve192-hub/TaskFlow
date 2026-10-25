#!/usr/bin/env python3
# Prepare disposable browser-test inputs from already packaged backend services.
# Select exactly one executable Boot JAR per module, generate fresh RSA/infra/Mongo
# credentials, and stage ignored bind-mount files consumed by .github/e2e/compose.yml.
# The host-private secrets directory allows container-readable fixture files without
# exposing them through Git. Runtime env output is mode 0600; CI masks generated
# passwords. This helper must never source a developer or production .env.

"""Stage executable JARs and generate credentials for the isolated E2E stack."""

import os
from pathlib import Path
import secrets
import shutil
import subprocess
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[2]
STACK = ROOT / ".github/e2e"
MODULES = {
    "discovery-server": "infrastructure/discovery-server",
    "config-server": "infrastructure/config-server",
    "api-gateway": "infrastructure/api-gateway",
    "auth-service": "services/identity/auth-service",
    "user-service": "services/identity/user-service",
    "project-service": "services/work-management/project-service",
    "task-service": "services/work-management/task-service",
}


# Stage executable artifacts first, then generate isolated credentials/keys and
# runtime env files. Re-running rotates fixture credentials; use a disposable stack.
def main():
    jars = STACK / "jars"
    jars.mkdir(parents=True, exist_ok=True)
    for service, module in MODULES.items():
        candidates = list((ROOT / "backend" / module / "target").glob("*.jar"))
        executable = []
        for jar in candidates:
            with ZipFile(jar) as archive:
                if any(name.startswith("BOOT-INF/lib/") for name in archive.namelist()):
                    executable.append(jar)
        if len(executable) != 1:
            raise SystemExit(f"Expected one executable JAR for {service}; run Maven clean package first")
        staged = jars / f"{service}.jar"
        shutil.copyfile(executable[0], staged)
        staged.chmod(0o644)  # The runtime UID differs from the host build user.

    credentials = {
        "TASKFLOW_E2E_MONGO_PASSWORD": secrets.token_hex(24),
        "TASKFLOW_E2E_INFRA_PASSWORD": secrets.token_hex(24),
    }
    key_directory = STACK / "secrets"
    key_directory.mkdir(mode=0o700, exist_ok=True)
    key_directory.chmod(0o700)
    private = key_directory / "jwt.private-key"
    public = key_directory / "jwt.public-key"
    for path in [private, public]:
        if path.exists():
            path.chmod(0o600)
    subprocess.run(["openssl", "genpkey", "-algorithm", "RSA", "-pkeyopt", "rsa_keygen_bits:3072", "-out", str(private)], check=True, capture_output=True)
    subprocess.run(["openssl", "pkey", "-in", str(private), "-pubout", "-out", str(public)], check=True, capture_output=True)
    # The private parent directory is host-private; only auth receives this bind mount.
    private.chmod(0o444)
    public.chmod(0o444)
    files = {
        "spring.security.user.name": "taskflow-infra",
        "spring.security.user.password": credentials["TASKFLOW_E2E_INFRA_PASSWORD"],
        "spring.cloud.config.username": "taskflow-infra",
        "spring.cloud.config.password": credentials["TASKFLOW_E2E_INFRA_PASSWORD"],
        "client.properties": "spring.cloud.config.username=taskflow-infra\n"
            + f"spring.cloud.config.password={credentials['TASKFLOW_E2E_INFRA_PASSWORD']}\n"
            + "spring.config.import=configserver:http://config-server:8888\n",
    }
    for service in ["auth", "user", "project", "task"]:
        database = "taskflow-auth" if service == "auth" else f"taskflow-{service}s"
        files[f"{service}.mongodb.uri"] = f"mongodb://e2e:{credentials['TASKFLOW_E2E_MONGO_PASSWORD']}@mongodb:27017/{database}?authSource=admin"
    for filename, value in files.items():
        path = key_directory / filename
        if path.exists():
            path.chmod(0o600)
        path.write_text(value)
        path.chmod(0o444)
    env = STACK / ".env.runtime"
    # Restrict access before writing, including when replacing an existing file.
    descriptor = os.open(env, os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600)
    os.chmod(env, 0o600)
    with os.fdopen(descriptor, "w") as handle:
        handle.write("".join(f"{name}={value}\n" for name, value in credentials.items()))
    if os.environ.get("GITHUB_ACTIONS") == "true":
        for value in credentials.values():
            print(f"::add-mask::{value}")
    print(f"Staged {len(MODULES)} executable JARs and generated temporary E2E credentials")


if __name__ == "__main__":
    main()
