// 
package com.taskflow.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Starts the internal Eureka registry used by gateway load balancing and Feign clients.
 * application.yaml configures this standalone registry; application services register their own
 * reachable instance addresses. Imported profile-specific security protects production/CI
 * registration and discovery traffic. The registry and its dashboard are not public app APIs.
 */
@EnableEurekaServer
@SpringBootApplication
@Import({com.taskflow.security.InfrastructureSecurity.class, com.taskflow.security.DevelopmentInfrastructureSecurity.class, com.taskflow.security.EurekaClientAuthentication.class})
public class DiscoveryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
