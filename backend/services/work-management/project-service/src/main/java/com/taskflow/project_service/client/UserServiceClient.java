package com.taskflow.project_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/internal/users/{authUserId}/exists")
    boolean userExists(
            @PathVariable("authUserId")
            String authUserId
    );
}