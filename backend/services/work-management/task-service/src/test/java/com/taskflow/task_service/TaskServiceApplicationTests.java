package com.taskflow.task_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Failsafe context smoke test for task-service under isolated test dependency configuration.
 * Dedicated unit tests cover lifecycle/events, while TaskRepositoryIT verifies actual Mongo queries
 * and full-stack E2E checks the running Kafka/service collaboration path.
 */
@SpringBootTest
@TestPropertySource(properties = {
		"taskflow.kafka.topics.task-status-changed=task.status.changed.v1",
		"taskflow.kafka.topics.task-completed=task.completed.v1"
})
class TaskServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
