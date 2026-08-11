// 
// 
package com.taskflow.user_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;


@Configuration
@EnableMongoAuditing
public class MongoConfig {
    
    // @CreatedDate
    // private Instant createdDate;

    // @LastModifiedDate
    // private Instant updatedAt;
}
