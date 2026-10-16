import importlib.util
import json
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location("summary", Path(__file__).with_name("summarize-trivy.py"))
summary = importlib.util.module_from_spec(spec)
spec.loader.exec_module(summary)


class ReportRedactionTest(unittest.TestCase):
    def test_secret_values_and_source_are_removed_but_findings_remain(self):
        secret = "synthetic-credential-for-redaction"
        report = {"Results": [{"Target": "example.conf", "Secrets": [{
            "RuleID": "test-rule", "Severity": "HIGH", "StartLine": 7,
            "Match": secret, "Code": {"Lines": [{"Content": secret}]}},
        ], "Misconfigurations": [{"Severity": "HIGH", "ID": "config-test", "Message": f"Source contains {secret}"}]}]}
        redacted = summary.redact(report)
        self.assertNotIn(secret, json.dumps(redacted))
        finding = redacted["Results"][0]["Secrets"][0]
        self.assertEqual(finding["StartLine"], 7)
        self.assertEqual(finding["Severity"], "HIGH")
        self.assertNotIn("Code", finding)
        self.assertIn("test-rule", summary.summarize(redacted))
        self.assertIn("example.conf:7", summary.summarize(redacted))

    def test_dependency_findings_are_preserved(self):
        report = {"Results": [{"Target": "pom.xml", "Vulnerabilities": [{"VulnerabilityID": "example-CVE", "Severity": "CRITICAL", "PkgName": "example", "InstalledVersion": "1.0", "FixedVersion": "1.1"}]}]}
        self.assertEqual(summary.redact(report), report)
        self.assertIn("1 distinct findings", summary.summarize(report))

    def test_empty_reports_remain_valid(self):
        self.assertEqual(summary.redact({}), {})
        self.assertIn("No high or critical findings", summary.summarize({}))


if __name__ == "__main__":
    unittest.main()
