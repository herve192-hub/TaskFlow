package com.taskflow.task_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Starts task CRUD, assignment, comment, lifecycle, and event-publication components.
 * Feign clients resolve project/user services through Eureka; task data has its own MongoDB
 * store. Application configuration supplies Kafka topics/transport and shared production guards.
 */
@SpringBootApplication
@EnableFeignClients
public class TaskServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}
