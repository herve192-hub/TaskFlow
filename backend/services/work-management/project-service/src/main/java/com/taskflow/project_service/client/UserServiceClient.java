package com.taskflow.project_service.client;

import com.taskflow.project_service.client.config.FeignClientConfig;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign contract for validating that a proposed member has a user-service profile.
 * Uses canonical authUserId and Eureka's user-service name; FeignClientConfig forwards the
 * current caller's credential and maps downstream failures. Profile existence alone does not
 * grant project membership or prove that a profile is ACTIVE.
 */
@FeignClient(
        name = "user-service",
        configuration = FeignClientConfig.class
)
public interface UserServiceClient {

    @GetMapping("/internal/users/{authUserId}/exists")
    boolean userExists(
            @PathVariable("authUserId")
            String authUserId
    );
}
