"""Security regression tests mutate the real rendered Compose configuration."""

from copy import deepcopy
from pathlib import Path
import runpy
import unittest

policy = runpy.run_path(str(Path(__file__).with_name("verify-production-compose.py")))


class ProductionPolicyTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.config = policy["render"]()
        policy["validate"](cls.config)

    def rejected(self, change):
        config = deepcopy(self.config)
        change(config)
        with self.assertRaises(ValueError):
            policy["validate"](config)

    def test_normal_and_bootstrap_deployments_pass(self):
        policy["validate"](self.config)
        policy["validate"](policy["render"](True), True)

    def test_business_ports_cannot_be_published(self):
        self.rejected(lambda c: c["services"]["auth-service"].update(ports=[{"target": 8081, "published": "8081"}]))

    def test_container_privilege_and_resource_regressions_are_blocked(self):
        for field, value in [("user", "0:0"), ("read_only", False), ("cap_add", ["SYS_ADMIN"]),
                             ("privileged", True), ("mem_limit", 0), ("pids_limit", -1),
                             ("stop_grace_period", "10s"), ("security_opt", []), ("image", "taskflow/auth-service:latest")]:
            with self.subTest(field=field):
                self.rejected(lambda c: c["services"]["auth-service"].update({field: value}))

    def test_signing_keys_cannot_be_shared_with_other_services(self):
        self.rejected(lambda c: c["services"]["user-service"]["secrets"].append({"source": "jwt-private"}))

    def test_healthchecks_cannot_be_disabled(self):
        self.rejected(lambda c: c["services"]["auth-service"].update(healthcheck={"test": ["NONE"]}))

    def test_control_network_cannot_become_public(self):
        self.rejected(lambda c: c["networks"]["control"].update(internal=False))

    def test_production_profile_and_cookie_controls_cannot_be_disabled(self):
        for key, value in [("SPRING_PROFILES_ACTIVE", "default"), ("TASKFLOW_COOKIE_SECURE", "false"),
                           ("TASKFLOW_PUBLIC_REGISTRATION_ENABLED", "true"), ("SERVER_ERROR_INCLUDE_MESSAGE", "always")]:
            with self.subTest(key=key):
                self.rejected(lambda c: c["services"]["auth-service"]["environment"].update({key: value}))

    def test_required_database_secret_cannot_be_removed(self):
        self.rejected(lambda c: c["services"]["task-service"].update(secrets=[]))

    def test_bootstrap_credentials_cannot_leak_into_routine_deployments(self):
        self.rejected(lambda c: c["services"]["auth-service"]["secrets"].append({"source": "bootstrap-admin-password"}))

    def test_aws_normal_and_bootstrap_deployments_pass(self):
        for bootstrap in [False, True]:
            policy["validate"](policy["render"](bootstrap, aws=True), bootstrap, aws=True)

    def test_aws_cannot_publish_backend_ports_or_use_mutable_images(self):
        for service, change in [
            ("auth-service", {"ports": [{"target": 8081, "published": "8081"}]}),
            ("auth-service", {"image": "123456789012.dkr.ecr.us-east-1.amazonaws.com/taskflow/auth-service:latest"}),
            ("taskflow-web", {"ports": [{"target": 8443, "published": "443", "protocol": "tcp"}]}),
            ("taskflow-web", {"volumes": []}),
            ("auth-service", {"logging": {"driver": "json-file"}}),
        ]:
            with self.subTest(service=service, change=change):
                config = policy["render"](aws=True)
                config["services"][service].update(change)
                with self.assertRaises(ValueError):
                    policy["validate"](config, aws=True)


if __name__ == "__main__":
    unittest.main()
