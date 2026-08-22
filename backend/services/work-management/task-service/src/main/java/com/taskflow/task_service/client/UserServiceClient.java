package com.taskflow.task_service.client;

import com.taskflow.task_service.client.dto.UserClientResponse;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{userId}")
    UserClientResponse getUserById(
            @PathVariable("userId") String userId
    );

    @GetMapping("/api/v1/users/{userId}/exists")
    boolean userExists(
            @PathVariable("userId") String userId
    );
}