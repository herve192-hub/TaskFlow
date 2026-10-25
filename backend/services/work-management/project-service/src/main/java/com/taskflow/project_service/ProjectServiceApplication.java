package com.taskflow.project_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Starts project/team APIs and registers Feign clients for profile existence checks.
 * Service configuration selects the projects database, while Eureka resolves user-service.
 * The project service owns membership-derived access decisions consumed by task-service.
 */
@SpringBootApplication
@EnableFeignClients
public class ProjectServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                ProjectServiceApplication.class,
                args
        );
    }
}
