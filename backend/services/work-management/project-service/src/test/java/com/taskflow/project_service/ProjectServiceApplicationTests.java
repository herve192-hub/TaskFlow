package com.taskflow.project_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Failsafe project-service context check using test MongoDB and disabled external discovery/config.
 * It checks bean wiring; role/ownership decisions and controller bindings have dedicated unit tests.
 */
@SpringBootTest
class ProjectServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
