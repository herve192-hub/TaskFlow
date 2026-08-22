package com.taskflow.task_service.client;

import com.taskflow.task_service.client.dto.ProjectAccessResponse;

import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "project-service")
public interface ProjectServiceClient {

    @GetMapping("/api/v1/projects/{projectId}/exists")
    boolean projectExists(
            @PathVariable("projectId") String projectId
    );

    @GetMapping("/api/v1/projects/{projectId}/access")
    ProjectAccessResponse getProjectAccess(
            @PathVariable("projectId") String projectId
    );

    @GetMapping("/api/v1/projects/{projectId}/members/{userId}/exists")
    boolean isProjectMember(
            @PathVariable("projectId") String projectId,
            @PathVariable("userId") String userId
    );
}