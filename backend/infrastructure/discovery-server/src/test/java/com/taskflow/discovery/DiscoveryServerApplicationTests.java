package com.taskflow.discovery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Failsafe context smoke test for the Eureka application with test-time external dependencies disabled.
 * It verifies bean startup rather than remote registrations, production credential distribution,
 * or host-network exposure; the Compose policy/E2E suites cover those separate contracts.
 */
@SpringBootTest
class DiscoveryServerApplicationTests {

	@Test
	void contextLoads() {
	}

}
