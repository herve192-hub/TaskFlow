package com.taskflow.auth_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Auth Spring context smoke test executed by Failsafe with the configured test MongoDB/signing setup.
 * External Config Server/Eureka calls are disabled by the parent test plugin configuration.
 * Controller/service security boundaries have additional focused onboarding tests.
 */
@SpringBootTest
class AuthServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
