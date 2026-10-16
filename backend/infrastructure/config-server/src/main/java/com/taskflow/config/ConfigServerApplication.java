package com.taskflow.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.cloud.config.server.EnableConfigServer;

/** Serve the native configuration repository with profile-specific infrastructure authentication. */
@EnableConfigServer
@SpringBootApplication
@Import({com.taskflow.security.InfrastructureSecurity.class, com.taskflow.security.DevelopmentInfrastructureSecurity.class, com.taskflow.security.EurekaClientAuthentication.class})
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }

}
