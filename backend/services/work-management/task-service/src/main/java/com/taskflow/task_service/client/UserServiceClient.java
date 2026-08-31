package com.taskflow.task_service.client;

import com.taskflow.task_service.client.dto.UserClientResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/internal/users/{authUserId}")
    UserClientResponse getUser(
            @PathVariable("authUserId")
            String authUserId
    );

    @GetMapping("/internal/users/{authUserId}/exists")
    boolean userExists(
            @PathVariable("authUserId")
            String authUserId
    );
}