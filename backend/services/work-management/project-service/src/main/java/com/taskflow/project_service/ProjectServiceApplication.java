package com.taskflow.project_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/** Start project/team APIs and enable user-service lookups through Feign. */
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
