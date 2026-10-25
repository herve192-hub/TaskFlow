package com.taskflow.task_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Enables auditing of task and comment creation/update timestamps through Spring Data.
 * It does not establish foreign keys to project/user data or an atomic MongoDB-to-Kafka write.
 * TaskServiceImpl owns writes and the event listener coordinates best-effort broker publication.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
