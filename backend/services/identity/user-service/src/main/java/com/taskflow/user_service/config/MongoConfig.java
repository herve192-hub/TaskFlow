// 
// 
package com.taskflow.user_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;


/**
 * Enables Spring Data auditing for profile creation/update timestamps.
 * User's @CreatedDate/@LastModifiedDate fields are populated when repositories persist entities.
 * This configuration neither creates cross-service transactions nor connects to auth's database.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
    
    // @CreatedDate
    // private Instant createdDate;

    // @LastModifiedDate
    // private Instant updatedAt;
}
