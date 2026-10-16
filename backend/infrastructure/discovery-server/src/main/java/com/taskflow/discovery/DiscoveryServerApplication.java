// 
package com.taskflow.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
@Import({com.taskflow.security.InfrastructureSecurity.class, com.taskflow.security.DevelopmentInfrastructureSecurity.class, com.taskflow.security.EurekaClientAuthentication.class})
public class DiscoveryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
