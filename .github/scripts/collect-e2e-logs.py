"""Collect isolated test logs without retaining credentials or JWT values."""

from pathlib import Path
import re
import subprocess

ROOT = Path(__file__).resolve().parents[2]
STACK = ROOT / ".github/e2e"


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
