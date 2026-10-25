package com.taskflow.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Starts Spring Cloud Config Server using the native repository configured in application.yaml.
 * Compose mounts backend/config-repo read-only; clients request service-named configuration
 * at startup. Imported infrastructure security selects Basic authentication for production/CI
 * and the loopback-only development chain otherwise. Runtime secret values stay on clients.
 */
@EnableConfigServer
@SpringBootApplication
@Import({com.taskflow.security.InfrastructureSecurity.class, com.taskflow.security.DevelopmentInfrastructureSecurity.class, com.taskflow.security.EurekaClientAuthentication.class})
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }

}
