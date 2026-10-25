package com.taskflow.project_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Enables creation/update/join timestamp auditing on project and membership documents.
 * It does not create foreign keys to auth/user/task stores or configure a distributed transaction.
 * Persistence behavior remains owned by the repositories and ProjectServiceImpl.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}

