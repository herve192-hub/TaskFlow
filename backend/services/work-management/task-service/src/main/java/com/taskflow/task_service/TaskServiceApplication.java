package com.taskflow.task_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/** Start task APIs, project/user Feign clients, and the configured task-event publisher. */
@SpringBootApplication
@EnableFeignClients
public class TaskServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}
