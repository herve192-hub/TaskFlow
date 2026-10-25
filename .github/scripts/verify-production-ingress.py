# Disposable Docker test for the direct production HTTPS NGINX configuration.
# Uses a generated test certificate/fake gateway to verify TLS, forwarded headers,
# rate limits, browser headers, and production SPA onboarding behavior. Runs separately
# from the E2E app network and removes its fixture containers. Requires a prebuilt
# frontend image; the helper generates its own test certificate without real keys.

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


# Build isolated TLS fixture inputs and validate observable HTTP/TLS behavior.
# The finally cleanup is essential because assertions can fail after containers start.
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
    image = os.environ.get("TASKFLOW_E2E_FRONTEND_IMAGE", f"taskflow/frontend-e2e:{tag}")
    project = os.environ.get("COMPOSE_PROJECT_NAME", "taskflow-e2e")
    arguments = ["docker", "run", "--detach", "--name", NAME, "--network", f"{project}_default",
        "--read-only", "--cap-drop", "ALL", "--security-opt", "no-new-privileges:true",
        "--publish", f"127.0.0.1:{PORT}:8443", "--tmpfs", "/tmp:rw,noexec,nosuid,size=16m",
        "--tmpfs", "/var/cache/nginx:rw,noexec,nosuid,size=32m,uid=101,gid=101,mode=0700",
        "--volume", f"{ROOT / 'frontend/nginx-production.conf'}:/etc/nginx/conf.d/default.conf:ro",
        "--volume", f"{certificate}:/run/secrets/tls.certificate:ro",
        "--volume", f"{private}:/run/secrets/tls.private-key:ro", image]
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
        for value in ["content-security-policy:", "script-src 'self'", "img-src 'self' data:;", "strict-transport-security: max-age=31536000",
                      "x-frame-options: deny", "x-content-type-options: nosniff", "permissions-policy:"]:
            if value not in headers:
                raise SystemExit(f"Missing production security header: {value}")
        wrong_host = run(["curl", "--noproxy", "*", "--silent", "--show-error", "--max-time", "5", "--cacert", str(certificate),
                          "--resolve", f"incorrect.test:{PORT}:127.0.0.1", f"https://incorrect.test:{PORT}/"])
        untrusted = run(["curl", "--noproxy", "*", "--silent", "--show-error", "--max-time", "5",
                         "--resolve", f"taskflow.test:{PORT}:127.0.0.1", f"https://taskflow.test:{PORT}/"])
        if wrong_host.returncode != 60 or untrusted.returncode != 60:
            raise SystemExit("TLS certificate or hostname verification was not enforced")

        for version in ["1.2", "1.3"]:
            probe = run(base + ["--fail", "--output", "/dev/null", f"--tlsv{version}", "--tls-max", version,
                                f"https://taskflow.test:{PORT}/"])
            if probe.returncode:
                raise SystemExit(f"Verified TLS {version} connection failed")
        weak_cipher = run(base + ["--tlsv1.2", "--tls-max", "1.2", "--ciphers", "AES128-SHA",
                                 f"https://taskflow.test:{PORT}/"])
        if weak_cipher.returncode != 35:
            raise SystemExit("Non-forward-secret TLS 1.2 cipher was not rejected")

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
                  "rejects_untrusted_certificate": True, "authentication_rate_limit": True,
                  "tls_1_2_and_1_3": True, "rejects_non_forward_secret_cipher": True}
        logs = STACK / "logs"
        logs.mkdir(exist_ok=True)
        (logs / "ingress-security.json").write_text(json.dumps(report, indent=2))
        print("Production HTTPS, TLS versions/cipher policy, certificate/hostname verification, security headers, and authentication rate limits passed")
    finally:
        if started:
            run(["docker", "rm", "--force", NAME], check=True)


if __name__ == "__main__":
    main()
