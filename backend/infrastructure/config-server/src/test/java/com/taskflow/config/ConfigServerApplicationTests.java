package com.taskflow.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Failsafe context smoke test for Config Server wiring under isolated test configuration.
 * Checks application startup, not the production file mounts, Basic credentials, remote clients,
 * or external secret-provider access; those require the deployment/E2E fixtures.
 */
@SpringBootTest
class ConfigServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
