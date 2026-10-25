#!/usr/bin/env python3
# AWS release/deployment orchestration alongside Terraform-provisioned resources.
# Terraform outputs supply non-secret settings; Secrets Manager supplies runtime
# credentials only on EC2. publish requires reviewed committed source, pushes eight
# amd64 images to ECR, resolves immutable digests, then uploads an allowlisted S3
# bundle. deploy validates that bundle, serializes host changes, stages restricted
# secret files, and waits for the production+AWS Compose stack to become healthy.
# Successful installs update /opt/taskflow/current; failed installs do not offer an
# automatic rollback. Host secret directories remain for mounted files and require
# operator lifecycle management. Never print captured AWS/Compose response bodies.

"""Publish a reviewed AWS release, or install its digest-pinned Compose stack on EC2.

Uses Python's standard library, AWS CLI v2, and Docker Compose 2.24.4+.
Secret values never enter command arguments, Terraform, images, or release archives.
"""

import argparse
from contextlib import contextmanager
import fcntl
import ipaddress
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import tarfile
import tempfile

ROOT = Path(__file__).resolve().parents[2]
HOST = Path("/opt/taskflow")
DOCKERFILES = {
    "frontend": ("frontend", "Dockerfile"),
    "discovery-server": ("backend", "infrastructure/discovery-server/Dockerfile"),
    "config-server": ("backend", "infrastructure/config-server/Dockerfile"),
    "api-gateway": ("backend", "infrastructure/api-gateway/Dockerfile"),
    "auth-service": ("backend", "services/identity/auth-service/Dockerfile"),
    "user-service": ("backend", "services/identity/user-service/Dockerfile"),
    "project-service": ("backend", "services/work-management/project-service/Dockerfile"),
    "task-service": ("backend", "services/work-management/task-service/Dockerfile"),
}
APPLICATION_KEYS = {
    "jwt.public-key", "jwt.private-key", "spring.security.user.name", "spring.security.user.password",
    "auth.mongodb.uri", "user.mongodb.uri", "project.mongodb.uri", "task.mongodb.uri",
    "spring.kafka.bootstrap-servers", "spring.kafka.properties.security.protocol",
    "spring.kafka.properties.sasl.mechanism", "spring.kafka.properties.sasl.jaas.config",
}
BOOTSTRAP_KEYS = {"taskflow.bootstrap.admin.email", "taskflow.bootstrap.admin.password"}
RELEASE_FILES = [
    "deploy/production/compose.yml", "deploy/production/compose.bootstrap.yml",
    "deploy/aws/compose.yml", "deploy/aws/nginx.conf", "deploy/aws/taskflow.py",
]


# Run from the release root and hide command output by default. Use visible only
# for non-secret image build/push output; errors disclose command name/exit code only.
def run(command, *, env=None, input_data=None, visible=False):
    result = subprocess.run(command, cwd=ROOT, env=env, input=input_data,
                            stdout=None if visible else subprocess.PIPE,
                            stderr=None if visible else subprocess.PIPE, check=False)
    if result.returncode:
        # AWS errors and Compose rendering can contain credentials. Do not echo them.
        raise ValueError(f"{command[0]} command failed (exit {result.returncode})")
    return result.stdout


# Consistent AWS CLI JSON calls without a pager. Credentials come from the normal
# CLI chain (the EC2 instance role on hosts), never Terraform or command-line secrets.
def aws(region, *arguments):
    return run(["aws", "--region", region, "--no-cli-pager", "--output", "json", *arguments])


# Validate Terraform-output shape and destinations before any release/host work.
# Origin, registry/account/region, and narrow ALB subnet trust must agree. This is
# input validation, not an AWS permission or resource-existence check.
def settings_valid(settings):
    required = {"region", "origin", "application_secret_arn", "log_group", "alb_subnet_cidrs",
                "ecr_repositories", "release_bucket"}
    if not required <= settings.keys():
        raise ValueError("Missing deployment settings; use terraform output -json deployment")
    if not re.fullmatch(r"[a-z]{2}(?:-[a-z]+)+-\d", settings["region"]):
        raise ValueError("Invalid AWS region")
    if not re.fullmatch(r"https://[a-z0-9][a-z0-9.-]+\.[a-z]{2,}", settings["origin"]):
        raise ValueError("The origin must be the exact HTTPS hostname without a trailing slash")
    if not settings["log_group"].startswith("/taskflow/"):
        raise ValueError("Use the provisioned TaskFlow log group")
    if not settings["application_secret_arn"].startswith("arn:"):
        raise ValueError("Use the complete application secret ARN")
    cidrs = settings["alb_subnet_cidrs"]
    if not isinstance(cidrs, list) or len(cidrs) < 2:
        raise ValueError("Provide the ALB subnet CIDRs from Terraform")
    for value in cidrs:
        network = ipaddress.ip_network(value)
        if network.version != 4 or network.prefixlen < 16:
            raise ValueError("ALB trust must be limited to individual IPv4 subnet CIDRs (/16 or narrower)")
    repositories = settings["ecr_repositories"]
    if set(repositories) != set(DOCKERFILES):
        raise ValueError("The release must include all eight application images")
    for name, repository in repositories.items():
        pattern = rf"\d{{12}}\.dkr\.ecr\.{re.escape(settings['region'])}\.amazonaws\.com(?:\.cn)?/[a-z0-9/-]+/{name}"
        if not re.fullmatch(pattern, repository):
            raise ValueError(f"Invalid ECR repository for {name}")
    if len({url.split("/")[0] for url in repositories.values()}) != 1:
        raise ValueError("All images must use the same ECR registry")


# Require a known schema/release ID and exactly eight expected repository digests.
# The manifest is a reviewed deployment input; this does not cryptographically sign it.
def manifest_valid(manifest):
    if manifest.get("version") != 1 or not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9._-]{0,63}", manifest.get("release", "")):
        raise ValueError("Invalid release manifest version or ID")
    settings_valid(manifest["deployment"])
    images = manifest.get("images", {})
    repositories = manifest["deployment"]["ecr_repositories"]
    if set(images) != set(DOCKERFILES):
        raise ValueError("Missing release images")
    for name, image in images.items():
        if not re.fullmatch(re.escape(repositories[name]) + r"@sha256:[a-f0-9]{64}", image):
            raise ValueError(f"Release image {name} must use its expected ECR repository and SHA-256 digest")


# Temporary Docker config keeps the short-lived ECR credential out of persistent
# user config; cleanup occurs on both success and failure of the enclosing operation.
@contextmanager
def docker_login(settings):
    # Keep the short-lived ECR token out of the user's persistent Docker config.
    with tempfile.TemporaryDirectory(prefix="taskflow-docker-") as directory:
        environment = {**os.environ, "DOCKER_CONFIG": directory, "AWS_REGION": settings["region"]}
        registry = settings["ecr_repositories"]["frontend"].split("/")[0]
        token = aws(settings["region"], "ecr", "get-login-password")
        run(["docker", "login", "--username", "AWS", "--password-stdin", registry],
            env=environment, input_data=token)
        yield environment


# Mutation boundary: build/push all images, then upload the allowlisted release.
# Require a clean Git tree for a reviewable source revision. Partial failure may
# leave published images; S3 upload happens only after the full manifest validates.
def publish(args):
    if not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9._-]{0,63}", args.release):
        raise ValueError("Release ID must be a Docker-compatible tag of at most 64 characters")
    settings = json.loads(Path(args.settings).read_text())
    settings_valid(settings)
    if run(["git", "status", "--porcelain", "--untracked-files=normal"]).strip():
        raise ValueError("Commit the reviewed source changes before publishing a release")
    commit = run(["git", "rev-parse", "HEAD"]).decode().strip()
    images = {}
    with docker_login(settings) as environment:
        for name, (context, dockerfile) in DOCKERFILES.items():
            print(f"Building and publishing {name}", flush=True)
            repository = settings["ecr_repositories"][name]
            tag = f"{repository}:{args.release}"
            command = ["docker", "build", "--platform", "linux/amd64", "--file", str(ROOT / context / dockerfile),
                       "--label", f"org.opencontainers.image.revision={commit}", "--tag", tag]
            if name == "frontend":
                command += ["--build-arg", "VITE_API_BASE_URL=/", "--build-arg", "VITE_PUBLIC_REGISTRATION_ENABLED=false"]
            run(command + [str(ROOT / context)], env=environment, visible=True)
            run(["docker", "push", tag], env=environment, visible=True)
            response = json.loads(aws(settings["region"], "ecr", "describe-images", "--repository-name",
                                      repository.split("/", 1)[1], "--image-ids", f"imageTag={args.release}"))
            images[name] = f"{repository}@{response['imageDetails'][0]['imageDigest']}"
    manifest = {"version": 1, "release": args.release, "commit": commit, "deployment": settings, "images": images}
    manifest_valid(manifest)
    # An explicit allowlist prevents local .env files, credentials, or build output entering the bundle.
    with tempfile.TemporaryDirectory(prefix="taskflow-release-") as directory:
        staging = Path(directory)
        files = RELEASE_FILES + [str(path.relative_to(ROOT)) for path in sorted((ROOT / "backend/config-repo").glob("*.yml"))]
        for name in files:
            source = ROOT / name
            if source.is_symlink():
                raise ValueError("Release source files must not be symlinks")
            destination = staging / name
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(source, destination)
        (staging / "release.json").write_text(json.dumps(manifest, indent=2) + "\n")
        archive = staging / "release.tgz"
        with tarfile.open(archive, "w:gz") as bundle:
            for name in files + ["release.json"]:
                bundle.add(staging / name, arcname=name, recursive=False)
        key = f"s3://{settings['release_bucket']}/releases/{args.release}.tgz"
        aws(settings["region"], "s3", "cp", str(archive), key, "--only-show-errors")
    print(f"Published {key} (source {commit}). Install it on EC2 with the deployment guide.")


# Read one Secrets Manager JSON value and require the exact documented key set.
# Reject empty/non-string/NUL values without reporting them in exception output.
def read_secret(region, arn, expected_keys):
    response = json.loads(aws(region, "secretsmanager", "get-secret-value", "--secret-id", arn))
    values = json.loads(response["SecretString"])
    if not isinstance(values, dict) or set(values) != expected_keys:
        raise ValueError("Secret JSON must contain exactly the documented property names")
    if any(not isinstance(value, str) or not value.strip() or "\0" in value for value in values.values()):
        raise ValueError("Secret properties must be nonempty strings without NUL characters")
    return values


# Escape credentials for Java .properties syntax before writing Config Server
# client settings; raw backslashes, separators, whitespace, and Unicode must round-trip.
def property_escape(value):
    # Java properties escaping, including UTF-16 code units for non-ASCII characters.
    result = []
    for char in value:
        if char in "\\:=#! ":
            result.append("\\" + char)
        elif char in "\n\r\t\f":
            result.append({"\n": "\\n", "\r": "\\r", "\t": "\\t", "\f": "\\f"}[char])
        elif ord(char) > 126 or ord(char) < 32:
            encoded = char.encode("utf-16-be")
            result.extend(f"\\u{int.from_bytes(encoded[index:index + 2], 'big'):04x}" for index in range(0, len(encoded), 2))
        else:
            result.append(char)
    return "".join(result)


# Materialize configtree property files for container UID/GID 10001 under a private
# host directory. Derive protected Config Server properties and credential-free
# Eureka URL; generate separate read-only NGINX trust rules from validated ALB CIDRs.
def write_secrets(directory, values, settings):
    username = values["spring.security.user.name"]
    password = values["spring.security.user.password"]
    if not re.fullmatch(r"[A-Za-z0-9._-]+", username) or len(password) < 32 or any(c in password for c in "\r\n"):
        raise ValueError("Use a simple infrastructure username and a single-line password of at least 32 characters")
    values = dict(values)
    values["client.properties"] = (
        "spring.config.import=configserver:http://config-server:8888\n"
        f"spring.cloud.config.username={property_escape(username)}\n"
        f"spring.cloud.config.password={property_escape(password)}\n"
    )
    values["eureka.client.service-url.defaultZone"] = "http://discovery-server:8761/eureka/"
    for name, value in values.items():
        path = directory / name
        path.write_text(value)
        path.chmod(0o400)
        os.chown(path, 10001, 10001)
    trust = directory / "alb-trust.conf"
    trust.write_text("".join(f"set_real_ip_from {ipaddress.ip_network(cidr)};\n" for cidr in settings["alb_subnet_cidrs"]))
    trust.chmod(0o444)


# Select the explicit production base then AWS overlay with an empty env file.
# Append the one-time bootstrap overlay last only for initial admin provisioning.
def compose_command(bootstrap=False):
    command = ["docker", "compose", "--env-file", "/dev/null", "--project-name", "taskflow-production",
               "--file", str(ROOT / "deploy/production/compose.yml"), "--file", str(ROOT / "deploy/aws/compose.yml")]
    if bootstrap:
        command += ["--file", str(ROOT / "deploy/production/compose.bootstrap.yml")]
    return command


# Root-only EC2 operation guarded by cloud-init readiness and a nonblocking host
# lock. Validate/pull before recreating services; wait for health before moving the
# current-release symlink. Bootstrap mounts are removed by recreating auth first.
def deploy(args):
    if os.geteuid() != 0:
        raise ValueError("Run deployment with sudo on the prepared EC2 host")
    if not (HOST / "host-ready").is_file():
        raise ValueError("Wait for the EC2 cloud-init host setup to complete")
    manifest = json.loads((ROOT / "release.json").read_text())
    manifest_valid(manifest)
    settings = manifest["deployment"]
    # Serialize deployments, including concurrent SSM sessions, before reading/writing secrets.
    with open(HOST / "deploy.lock", "w") as lock:
        fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
        values = read_secret(settings["region"], settings["application_secret_arn"], APPLICATION_KEYS)
        if args.bootstrap:
            if not settings.get("bootstrap_secret_arn"):
                raise ValueError("Initial provisioning requires a separate bootstrap secret ARN")
            values.update(read_secret(settings["region"], settings["bootstrap_secret_arn"], BOOTSTRAP_KEYS))
        directory = Path(tempfile.mkdtemp(prefix=f"{manifest['release']}-", dir=HOST / "secrets"))
        directory.chmod(0o700)
        write_secrets(directory, values, settings)
        with docker_login(settings) as environment:
            environment = {key: value for key, value in environment.items() if not key.startswith("COMPOSE_")}
            environment.update(COMPOSE_DISABLE_ENV_FILE="1", TASKFLOW_WEB_ORIGIN=settings["origin"],
                               TASKFLOW_SECRETS_DIR=str(directory), TASKFLOW_LOG_GROUP=settings["log_group"])
            for name, image in manifest["images"].items():
                environment[f"TASKFLOW_{name.upper().replace('-', '_')}_IMAGE"] = image
            command = compose_command(args.bootstrap)
            run(command + ["config", "--quiet"], env=environment)
            print("Pulling all release images before changing running services", flush=True)
            run(command + ["pull", "--quiet"], env=environment)
            print("Starting services and waiting for health checks", flush=True)
            run(command + ["up", "--detach", "--no-build", "--pull", "never", "--force-recreate", "--wait", "--wait-timeout", "600"], env=environment)
            if args.bootstrap:
                # Recreate auth without the one-time mounts before deleting the host copies.
                run(compose_command() + ["up", "--detach", "--no-build", "--pull", "never", "--no-deps",
                                         "--force-recreate", "--wait", "--wait-timeout", "300", "auth-service"], env=environment)
                for name in BOOTSTRAP_KEYS:
                    (directory / name).unlink()
        temporary_link = HOST / "current.next"
        temporary_link.unlink(missing_ok=True)
        temporary_link.symlink_to(ROOT, target_is_directory=True)
        temporary_link.replace(HOST / "current")
    print(f"Installed {manifest['release']}. Verify {settings['origin']}/healthz and the sign-in flow.")


# Parse explicit publish/deploy actions and report sanitized failures. Do not
# append exception text/locals here: captured provider responses can contain secrets.
def main():
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    publication = commands.add_parser("publish", help="Build amd64 images, push to ECR, and upload the release to S3")
    publication.add_argument("--settings", required=True, help="JSON from terraform output -json deployment")
    publication.add_argument("--release", required=True, help="Unique immutable release ID, such as v1.0.0 or the commit SHA")
    installation = commands.add_parser("deploy", help="Install the extracted release on EC2")
    installation.add_argument("--bootstrap", action="store_true", help="Provision the first admin, then automatically remove its mounts")
    args = parser.parse_args()
    try:
        (publish if args.command == "publish" else deploy)(args)
    except (ValueError, OSError, KeyError, TypeError):
        # Never include response bodies, secret values, or exception locals in logs.
        raise SystemExit("TaskFlow deployment failed. Check settings, AWS permissions, image availability, and service health. Secret values were not printed.") from None


if __name__ == "__main__":
    main()
