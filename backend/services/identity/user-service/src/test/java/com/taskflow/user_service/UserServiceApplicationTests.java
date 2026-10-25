package com.taskflow.user_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Failsafe application-context check using isolated configuration and the test MongoDB connection.
 * It verifies profile-service wiring without Config Server/Eureka calls; focused controller/service
 * suites separately test profile ownership, directory privacy, avatars, and preferences.
 */
@SpringBootTest
class UserServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
