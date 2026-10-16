"""Verify the production NGINX TLS policy and headers using an isolated test certificate."""

from concurrent.futures import ThreadPoolExecutor
import json
import os
from pathlib import Path
import subprocess
import time

ROOT = Path(__file__).resolve().parents[2]
STACK = ROOT / ".github/e2e"
NAME = "taskflow-e2e-tls"
PORT = 19443


def run(arguments, **kwargs):
    return subprocess.run(arguments, cwd=ROOT, capture_output=True, text=True, **kwargs)


def main():
    directory = STACK / "secrets"
    directory.mkdir(mode=0o700, exist_ok=True)
    directory.chmod(0o700)
    certificate = directory / "test-tls.certificate"
    private = directory / "test-tls.private-key"
    for path in [certificate, private]:
        if path.exists():
            path.chmod(0o600)
    run(["openssl", "req", "-x509", "-newkey", "rsa:3072", "-nodes", "-days", "1",
         "-subj", "/CN=taskflow.test", "-addext", "subjectAltName=DNS:taskflow.test",
         "-keyout", str(private), "-out", str(certificate)], check=True)
    certificate.chmod(0o444)
    private.chmod(0o444)  # Only the private parent directory is traversable on the host.
    tag = os.environ.get("TASKFLOW_E2E_IMAGE_TAG", "0.0.0")
    project = os.environ.get("COMPOSE_PROJECT_NAME", "taskflow-e2e")
    arguments = ["docker", "run", "--detach", "--name", NAME, "--network", f"{project}_default",
        "--read-only", "--cap-drop", "ALL", "--security-opt", "no-new-privileges:true",
        "--publish", f"127.0.0.1:{PORT}:8443", "--tmpfs", "/tmp:rw,noexec,nosuid,size=16m",
        "--tmpfs", "/var/cache/nginx:rw,noexec,nosuid,size=32m,uid=101,gid=101,mode=0700",
        "--volume", f"{ROOT / 'frontend/nginx-production.conf'}:/etc/nginx/conf.d/default.conf:ro",
        "--volume", f"{certificate}:/run/secrets/tls.certificate:ro",
        "--volume", f"{private}:/run/secrets/tls.private-key:ro", f"taskflow/frontend-e2e:{tag}"]
    started = False
    try:
        run(arguments, check=True)
        started = True
        base = ["curl", "--noproxy", "*", "--silent", "--show-error", "--connect-timeout", "2", "--max-time", "5",
                "--resolve", f"taskflow.test:{PORT}:127.0.0.1", "--cacert", str(certificate)]
        response = None
        for _ in range(20):
            response = run(base + ["--include", f"https://taskflow.test:{PORT}/"])
            if response.returncode == 0:
                break
            time.sleep(0.25)
        if response is None or response.returncode:
            raise SystemExit("Verified production HTTPS did not become ready")
        headers = response.stdout.split("\r\n\r\n", 1)[0].split("\n\n", 1)[0].lower()
        for value in ["content-security-policy:", "script-src 'self'", "strict-transport-security: max-age=31536000",
                      "x-frame-options: deny", "x-content-type-options: nosniff", "permissions-policy:"]:
            if value not in headers:
                raise SystemExit(f"Missing production security header: {value}")
        wrong_host = run(["curl", "--noproxy", "*", "--silent", "--show-error", "--max-time", "5", "--cacert", str(certificate),
                          "--resolve", f"incorrect.test:{PORT}:127.0.0.1", f"https://incorrect.test:{PORT}/"])
        untrusted = run(["curl", "--noproxy", "*", "--silent", "--show-error", "--max-time", "5",
                         "--resolve", f"taskflow.test:{PORT}:127.0.0.1", f"https://taskflow.test:{PORT}/"])
        if wrong_host.returncode != 60 or untrusted.returncode != 60:
            raise SystemExit("TLS certificate or hostname verification was not enforced")

        def auth_request(_):
            result = run(base + ["--output", "/dev/null", "--write-out", "%{http_code}", "--header",
                          "Content-Type: application/json", "--data", "{}", f"https://taskflow.test:{PORT}/auth/login"])
            if result.returncode:
                raise RuntimeError("Production authentication limit probe failed")
            return result.stdout

        with ThreadPoolExecutor(max_workers=20) as executor:
            statuses = list(executor.map(auth_request, range(20)))
        if "429" not in statuses:
            raise SystemExit("Authentication requests were not rate limited")
        report = {"verified_https": True, "security_headers": True, "rejects_wrong_hostname": True,
                  "rejects_untrusted_certificate": True, "authentication_rate_limit": True}
        logs = STACK / "logs"
        logs.mkdir(exist_ok=True)
        (logs / "ingress-security.json").write_text(json.dumps(report, indent=2))
        print("Production HTTPS, certificate/hostname verification, security headers, and authentication rate limits passed")
    finally:
        if started:
            run(["docker", "rm", "--force", NAME], check=True)


if __name__ == "__main__":
    main()
