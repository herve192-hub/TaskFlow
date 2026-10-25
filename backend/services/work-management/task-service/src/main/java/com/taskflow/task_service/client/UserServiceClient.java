package com.taskflow.task_service.client;

import com.taskflow.task_service.client.dto.UserClientResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign contract for resolving and validating canonical account-linked user profiles.
 * TaskServiceImpl currently uses the existence endpoint when checking an assignee. It must also
 * check project membership separately; profile existence does not imply assignment eligibility.
 */
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
