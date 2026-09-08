package com.taskflow.task_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

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
