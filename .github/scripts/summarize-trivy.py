"""Show actionable Trivy findings without exposing secret matches."""

import argparse
from collections import Counter
import html
import json
import os
import tempfile
from pathlib import Path


def redact(report):
    """Keep findings and locations while removing credential values from artifacts."""
    matches = set()
    for result in report.get("Results", []) or []:
        for finding in result.get("Secrets", []) or []:
            match = finding.pop("Match", None)
            if isinstance(match, str) and match:
                matches.add(match)
            finding.pop("Code", None)

    def clean(value):
        if isinstance(value, dict):
            return {key: clean(item) for key, item in value.items()}
        if isinstance(value, list):
            return [clean(item) for item in value]
        if isinstance(value, str):
            for match in sorted(matches, key=len, reverse=True):
                value = value.replace(match, "[REDACTED]")
        return value

    return clean(report)


def summarize(report):
    findings = {}
    for result in report.get("Results", []) or []:
        result_target = result.get("Target", "unknown file")
        for category, key in (
            ("Dependency", "Vulnerabilities"),
            ("Configuration", "Misconfigurations"),
            ("Secret", "Secrets"),
        ):
            for finding in result.get(key, []) or []:
                target = result_target
                if key == "Misconfigurations" and finding.get("Status", "FAIL") != "FAIL":
                    continue
                severity = finding.get("Severity", "UNKNOWN")
                if severity not in ("HIGH", "CRITICAL"):
                    continue
                identifier = finding.get("VulnerabilityID") or finding.get("AVDID") or finding.get("RuleID") or finding.get("ID", "unknown")
                if category == "Dependency":
                    target = finding.get("PkgPath") or result_target
                    package = finding.get("PkgName", "unknown package")
                    installed = finding.get("InstalledVersion", "unknown")
                    fix = finding.get("FixedVersion") or "No published fixed version"
                elif category == "Configuration":
                    package = finding.get("Title", "Configuration finding")
                    installed = ""
                    fix = finding.get("Message", "Review the configuration")
                else:
                    # Never include Match, Code, or other fields containing secret values.
                    package = "Secret detected"
                    installed = ""
                    fix = "Remove and rotate the credential"
                    target = f"{result.get('Target', 'unknown file')}:{finding.get('StartLine', '?')}"
                identity = (category, severity, identifier, package, installed, fix)
                findings.setdefault(identity, set()).add(target)

    lines = ["## Trivy security findings", ""]
    if not findings:
        return "\n".join(lines + ["No high or critical findings.", ""])
    counts = Counter(identity[1] for identity in findings)
    lines += [f"{len(findings)} distinct findings: {counts['CRITICAL']} critical, {counts['HIGH']} high.", ""]
    lines += ["| Severity | Type | Finding | Package or check | Installed | Fix | Affected files |", "| --- | --- | --- | --- | --- | --- | --- |"]

    def cell(value):
        return html.escape(" ".join(str(value).split())).replace("|", "&#124;")

    for identity, targets in sorted(findings.items(), key=lambda item: (item[0][1] != "CRITICAL", item[0])):
        category, severity, identifier, package, installed, fix = identity
        columns = [severity, category, identifier, package, installed, fix]
        affected = "<br>".join(cell(target) for target in sorted(targets))
        lines.append("| " + " | ".join(cell(value) for value in columns) + " | " + affected + " |")
    return "\n".join(lines + [""])


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("report", type=Path)
    arguments = parser.parse_args()
    if not arguments.report.is_file():
        parser.exit(1, "Trivy did not produce a report; check the scanner log for an execution error.\n")
    report = redact(json.loads(arguments.report.read_text()))
    # Replace the raw report before either summarizing or uploading it.
    with tempfile.NamedTemporaryFile(mode="w", dir=arguments.report.parent, delete=False, encoding="utf-8") as handle:
        json.dump(report, handle)
        temporary = Path(handle.name)
    temporary.replace(arguments.report)
    output = summarize(report)
    print(output)
    if summary := os.environ.get("GITHUB_STEP_SUMMARY"):
        with Path(summary).open("a", encoding="utf-8") as handle:
            handle.write(output + "\n")


if __name__ == "__main__":
    main()
