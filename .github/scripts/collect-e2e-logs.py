# Collect only the isolated E2E project's status/log output before teardown.
# Generated runtime credentials, URL user-info, and JWT-shaped strings are redacted
# before writing ignored log artifacts. It does not collect arbitrary host logs or
# production secrets. Maintain redaction when adding new fixture credential sources.

"""Collect isolated test logs without retaining credentials or JWT values."""

from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[2]
STACK = ROOT / ".github/e2e"


# Skip absent runtime setup. Capture both stdout/stderr, redact before disk writes,
# and fail log collection without printing raw process output on command errors.
def main():
    directory = STACK / "logs"
    directory.mkdir(exist_ok=True)
    runtime = STACK / ".env.runtime"
    if not runtime.is_file():
        return
    credentials = [line.split("=", 1)[1] for line in runtime.read_text().splitlines() if "=" in line]
    compose = ["docker", "compose", "--project-name", "taskflow-e2e", "--env-file", str(runtime), "--file", str(STACK / "compose.yml")]
    for filename, command in [("containers.txt", ["ps", "--all"]), ("stack.log", ["logs", "--no-color"])]:
        result = subprocess.run(compose + command, cwd=ROOT, capture_output=True, text=True)
        output = result.stdout + result.stderr
        for credential in credentials:
            if credential:
                output = output.replace(credential, "[REDACTED]")
        output = re.sub(r"https?://[^\s/@:]+:[^\s/@]+@", "http://[REDACTED]@", output)
        output = re.sub(r"\beyJ[\w-]+\.[\w-]+\.[\w-]+", "[REDACTED-JWT]", output)
        (directory / filename).write_text(output)
        if result.returncode:
            raise SystemExit("Failed to collect isolated test logs")


if __name__ == "__main__":
    main()
