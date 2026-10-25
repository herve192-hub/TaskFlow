# Disposable Docker test for deploy/aws/nginx.conf and its trusted ALB boundary.
# A fake gateway and isolated network exercise HTTPS markers, client-address trust,
# rate limiting, SPA fallback, and health routing. Does not contact AWS or validate
# real security groups; Terraform/production policy own those boundaries. Fixture
# containers/networks are cleaned up even when an assertion fails.

"""Exercise the AWS NGINX configuration with synthetic ALB headers and a fake gateway."""

import json
import os
from pathlib import Path
import subprocess
import tempfile
import time
import uuid

ROOT = Path(__file__).resolve().parents[2]


def run(*arguments):
    return subprocess.check_output(arguments, text=True, stderr=subprocess.STDOUT).strip()


# Start the isolated fake ALB/gateway boundary, assert client trust and API behavior,
# then remove fixture resources through finally even when a check fails.
def main():
    image = os.environ["TASKFLOW_AWS_FRONTEND_IMAGE"]
    name = "taskflow-aws-check-" + uuid.uuid4().hex[:10]
    started = []
    run("docker", "network", "create", name)
    try:
        with tempfile.TemporaryDirectory(prefix="taskflow-alb-check-") as temporary:
            directory = Path(temporary)
            subnet = json.loads(run("docker", "network", "inspect", name))[0]["IPAM"]["Config"][0]["Subnet"]
            trust = directory / "trust.conf"
            trust.write_text(f"set_real_ip_from {subnet};\n")
            stub = directory / "gateway.conf"
            stub.write_text('''server {
                listen 8080;
                location = /actuator/health { return 200 '{"status":"UP"}'; }
                location / {
                    default_type application/json;
                    return 200 '{"proto":"$http_x_forwarded_proto","xff":"$http_x_forwarded_for","forwarded":"$http_forwarded","host":"$http_x_forwarded_host","port":"$http_x_forwarded_port","prefix":"$http_x_forwarded_prefix","ssl":"$http_x_forwarded_ssl"}';
                }
            }''')
            trust.chmod(0o444)
            stub.chmod(0o444)
            common = ["--read-only", "--user", "101:101", "--cap-drop", "ALL", "--security-opt", "no-new-privileges:true",
                      "--network", name, "--tmpfs", "/tmp:rw,noexec,nosuid,size=16m",
                      "--tmpfs", "/var/cache/nginx:rw,noexec,nosuid,size=32m,uid=101,gid=101,mode=0700"]
            gateway = name + "-gateway"
            run("docker", "run", "--detach", "--name", gateway, "--network-alias", "api-gateway", *common,
                "--volume", f"{stub}:/etc/nginx/conf.d/default.conf:ro", image)
            started.append(gateway)
            web = name + "-web"
            run("docker", "run", "--detach", "--name", web, *common, "--publish", "127.0.0.1::8080",
                "--volume", f"{ROOT / 'deploy/aws/nginx.conf'}:/etc/nginx/conf.d/default.conf:ro",
                "--volume", f"{trust}:/etc/nginx/taskflow-alb-trust.conf:ro", image)
            started.append(web)
            port = json.loads(run("docker", "inspect", web))[0]["NetworkSettings"]["Ports"]["8080/tcp"][0]["HostPort"]
            base = f"http://127.0.0.1:{port}"

            def request(path, headers=(), include=False):
                command = ["curl", "--noproxy", "*", "--silent", "--show-error", "--max-time", "5"]
                if include:
                    command.append("--include")
                for header in headers:
                    command += ["--header", header]
                return run(*command, "--write-out", "\n%{http_code}", base + path).rsplit("\n", 1)

            for attempt in range(40):
                try:
                    if request("/healthz")[1] == "200":
                        break
                except subprocess.CalledProcessError:
                    pass
                time.sleep(0.25)
            else:
                raise AssertionError("AWS ingress/gateway did not become healthy")

            assert request("/")[1] == "403", "Missing HTTPS forwarding must be rejected"
            assert request("/api/probe", ["X-Forwarded-Proto: http"])[1] == "403"
            assert request("/projects/fixture", ["X-Forwarded-Proto: https"])[1] == "200", "SPA deep links must work"
            response, status = request("/api/probe", ["X-Forwarded-Proto: https", "X-Forwarded-For: 198.51.100.99, 203.0.113.17",
                                                       "Host: taskflow.example.test", "X-Forwarded-Host: attacker.invalid",
                                                       "X-Forwarded-Port: 1234", "X-Forwarded-Prefix: /attacker", "X-Forwarded-Ssl: off",
                                                       "Forwarded: for=attacker;proto=http"], include=True)
            assert status == "200"
            for header in ["content-security-policy:", "strict-transport-security: max-age=31536000", "x-content-type-options: nosniff"]:
                assert header in response.lower(), f"Missing security header: {header}"
            payload = json.loads(response[response.index("{"):])
            assert payload == {"proto": "https", "xff": "203.0.113.17", "forwarded": "", "host": "taskflow.example.test",
                               "port": "", "prefix": "", "ssl": ""}, payload
            # Vary spoofed prefixes, preserving the address appended by the ALB.
            statuses = [request("/auth/login", ["X-Forwarded-Proto: https",
                                               f"X-Forwarded-For: 198.51.100.{index}, 203.0.113.18"])[1] for index in range(20)]
            assert "429" in statuses, "Spoofed XFF prefixes must not bypass per-client authentication limits"
            assert request("/auth/login", ["X-Forwarded-Proto: https", "X-Forwarded-For: 203.0.113.19"])[1] == "200", "Separate clients need independent rate limits"
            print("AWS ingress passed: gateway health, HTTPS forwarding, SPA routes, security headers, XFF spoofing, and per-client rate limits")
    finally:
        for container in reversed(started):
            run("docker", "rm", "--force", container)
        run("docker", "network", "rm", name)


if __name__ == "__main__":
    main()
