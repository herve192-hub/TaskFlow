#!/usr/bin/env python3
"""Enforce the repository's production isolation policy without reading real secret values."""

import json
import os
from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[2]
BUSINESS = {"auth-service", "user-service", "project-service", "task-service"}
INFRASTRUCTURE = {"config-server", "discovery-server"}
SERVICES = BUSINESS | INFRASTRUCTURE | {"api-gateway", "taskflow-web"}


def render(bootstrap=False, aws=False):
    # Ignore developer/CI Compose selection and env files; these are public fixture settings only.
    environment = {name: value for name, value in os.environ.items() if not name.startswith("COMPOSE_")}
    environment.update(COMPOSE_DISABLE_ENV_FILE="1", TASKFLOW_WEB_ORIGIN="https://taskflow.example.test",
                       TASKFLOW_SECRETS_DIR="/tmp/taskflow-production-policy-secrets", TASKFLOW_HTTPS_PORT="443")
    command = ["docker", "compose", "--env-file", "/dev/null", "--project-name", "taskflow-production-policy",
               "--file", "deploy/production/compose.yml"]
    if aws:
        environment.update(AWS_REGION="us-east-1", TASKFLOW_LOG_GROUP="/taskflow/production-policy")
        for name in ["frontend", *sorted(SERVICES - {"taskflow-web"})]:
            key = "TASKFLOW_" + name.upper().replace("-", "_") + "_IMAGE"
            environment[key] = f"123456789012.dkr.ecr.us-east-1.amazonaws.com/taskflow/{name}@sha256:" + "a" * 64
        command += ["--file", "deploy/aws/compose.yml"]
    if bootstrap:
        command += ["--file", "deploy/production/compose.bootstrap.yml"]
    result = subprocess.run(command + ["config", "--format", "json"], cwd=ROOT, env=environment,
                            check=True, capture_output=True, text=True)
    return json.loads(result.stdout)


def validate(config, bootstrap=False, aws=False):
    errors = []

    def require(condition, message):
        if not condition:
            errors.append(message)

    services = config.get("services", {})
    require(set(services) == SERVICES, "The production service inventory must be reviewed when it changes")
    require(config.get("networks", {}).get("control", {}).get("internal") is True,
            "The control network must remain internal")
    private_owners = {"jwt-private": "auth-service", "tls-private": "taskflow-web",
                      **{f"{service.split('-')[0]}-mongo": service for service in BUSINESS},
                      **{name: "task-service" for name in ["kafka-brokers", "kafka-protocol", "kafka-mechanism", "kafka-jaas"]},
                      "bootstrap-admin-email": "auth-service", "bootstrap-admin-password": "auth-service"}

    for name, service in services.items():
        web = name == "taskflow-web"
        expected_networks = {"edge"} if web else {"edge", "control"} if name == "api-gateway" else (
            {"control"} if name in INFRASTRUCTURE else {"control", "data-egress"})
        require(set(service.get("networks", {})) == expected_networks, f"{name}: unexpected network access")
        require(service.get("user") == ("101:101" if web else "10001:10001"), f"{name}: explicit non-root UID required")
        require(service.get("read_only") is True, f"{name}: filesystem must be read-only")
        require(service.get("cap_drop") == ["ALL"] and not service.get("cap_add"), f"{name}: drop all capabilities")
        require("no-new-privileges:true" in service.get("security_opt", []), f"{name}: privilege escalation must be disabled")
        require(not any(service.get(key) for key in ["privileged", "devices", "network_mode", "pid", "ipc"]),
                f"{name}: host namespaces/devices and privileged mode are forbidden")
        require(0 < int(service.get("mem_limit", 0)) <= 805306368, f"{name}: bounded memory required")
        require(0 < int(service.get("pids_limit", 0)) <= 256, f"{name}: bounded process count required")
        require(service.get("stop_grace_period") == ("30s" if web else "45s"), f"{name}: preserve the shutdown drain window")
        require(service.get("restart") == "unless-stopped", f"{name}: restart policy required")
        health = service.get("healthcheck", {})
        require(bool(health.get("test")) and health["test"][0] in {"CMD", "CMD-SHELL"} and not health.get("disable"),
                f"{name}: healthcheck required")
        logging = service.get("logging", {})
        if aws:
            options = logging.get("options", {})
            require(logging.get("driver") == "awslogs" and options.get("awslogs-region")
                    and options.get("awslogs-group", "").startswith("/taskflow/")
                    and options.get("mode") == "non-blocking" and options.get("max-buffer-size") == "4m",
                    f"{name}: CloudWatch logs with bounded buffers required")
            require(not service.get("build"), f"{name}: AWS hosts must pull release images without building")
        image = service.get("image", "")
        if aws:
            require(bool(re.fullmatch(r"\d{12}\.dkr\.ecr\.[a-z0-9-]+\.amazonaws\.com(?:\.cn)?/[\w/-]+@sha256:[a-f0-9]{64}", image)),
                    f"{name}: AWS images must use ECR SHA-256 digests")
        else:
            require(logging.get("driver") == "local"
                    and logging.get("options") == {"max-size": "20m", "max-file": "5"},
                    f"{name}: bounded log retention required")
        require(bool(re.search(r":\d+\.\d+\.\d+(?:[-.][\w.-]+)?$|@sha256:[a-f0-9]{64}$", image)),
                f"{name}: explicit release image version or digest required")
        require(all(volume.get("read_only") for volume in service.get("volumes", [])),
                f"{name}: host volume mounts must be read-only")

        ports = service.get("ports", [])
        require((len(ports) == 1 and ports[0].get("target") == (8080 if aws else 8443)
                 and ports[0].get("protocol") == "tcp" and (not aws or ports[0].get("published") == "8080"))
                if web else not ports, f"{name}: only the frontend HTTPS port may be published")
        secrets = {secret["source"] for secret in service.get("secrets", [])}
        for secret in secrets:
            if secret in private_owners:
                require(private_owners[secret] == name, f"{name}: private secret {secret} belongs to another service")
        if name in BUSINESS:
            require({"jwt-public", f"{name.split('-')[0]}-mongo", "eureka-client", "config-client-properties"} <= secrets,
                    f"{name}: scoped verification/database/infrastructure secrets required")
        if name == "auth-service":
            require("jwt-private" in secrets, "Auth signing key required")
            bootstrap_secrets = {"bootstrap-admin-email", "bootstrap-admin-password"}
            require(secrets & bootstrap_secrets == (bootstrap_secrets if bootstrap else set()),
                    "Bootstrap credentials must appear only in the initial provisioning override")
        if web:
            if aws:
                require(not secrets and not {"tls-certificate", "tls-private"} & config.get("secrets", {}).keys(),
                        "AWS TLS keys belong in ACM, not on EC2")
                require(any(volume.get("target") == "/etc/nginx/conf.d/default.conf"
                            and volume.get("source") == str(ROOT / "deploy/aws/nginx.conf")
                            for volume in service.get("volumes", [])), "AWS ALB ingress configuration required")
                require(health.get("test", [])[-1:] == ["http://localhost:8080/healthz"], "AWS gateway readiness check required")
            else:
                require({"tls-certificate", "tls-private"} <= secrets, "Frontend TLS secrets required")
                require(str(service.get("build", {}).get("args", {}).get("VITE_PUBLIC_REGISTRATION_ENABLED")).lower() == "false",
                        "Frontend public registration must be disabled")
            continue

        environment = service.get("environment", {})
        require("prod" in environment.get("SPRING_PROFILES_ACTIVE", "").split(","), f"{name}: prod profile required")
        require(environment.get("SERVER_SHUTDOWN") == "graceful"
                and environment.get("SPRING_LIFECYCLE_TIMEOUT_PER_SHUTDOWN_PHASE") == "30s", f"{name}: graceful shutdown required")
        require(environment.get("MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE") == "health"
                and environment.get("MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS") == "never"
                and environment.get("MANAGEMENT_ENDPOINT_HEALTH_SHOW_COMPONENTS") == "never", f"{name}: management details must remain private")
        require(all(environment.get(key) == "never" for key in ["SERVER_ERROR_INCLUDE_MESSAGE", "SERVER_ERROR_INCLUDE_BINDING_ERRORS", "SERVER_ERROR_INCLUDE_STACKTRACE"]),
                f"{name}: internal errors must not be exposed")
        if name not in INFRASTRUCTURE:
            require(environment.get("TASKFLOW_COOKIE_SECURE") == "true"
                    and environment.get("TASKFLOW_PUBLIC_REGISTRATION_ENABLED") == "false", f"{name}: secure cookies and approved onboarding required")

    if errors:
        raise ValueError("Production policy failed:\n" + "\n".join(errors))


def main():
    for aws in [False, True]:
        for bootstrap in [False, True]:
            validate(render(bootstrap, aws), bootstrap, aws)
    print("Production isolation, secret scope, resources, and onboarding passed (direct TLS + AWS; normal + bootstrap)")


if __name__ == "__main__":
    try:
        main()
    except (ValueError, OSError, subprocess.CalledProcessError) as error:
        # Compose failure output can contain environment values; print only our policy errors.
        raise SystemExit(str(error) if isinstance(error, ValueError) else "Unable to validate production Compose configuration")
