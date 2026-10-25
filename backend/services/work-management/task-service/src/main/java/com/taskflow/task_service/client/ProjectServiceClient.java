package com.taskflow.task_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.taskflow.task_service.client.dto.ProjectAccessResponse;

/**
 * Authenticated Feign contract for project existence, caller access, and assignee membership.
 * The access endpoint derives identity from the forwarded token, not a userId query parameter.
 * TaskServiceImpl consults this owning service before project-scoped operations rather than
 * embedding project memberships or trusting role claims supplied by the browser.
 */
@FeignClient(name = "project-service")
public interface ProjectServiceClient {

    @GetMapping("/internal/projects/{projectId}/exists")
    boolean projectExists(
            @PathVariable("projectId")
            String projectId
    );

    @GetMapping("/internal/projects/{projectId}/access")
    ProjectAccessResponse getProjectAccess(
            @PathVariable("projectId")
            String projectId
    );

    @GetMapping(
            "/internal/projects/{projectId}/members/{userId}/exists"
    )
    boolean isProjectMember(
            @PathVariable("projectId")
            String projectId,

            @PathVariable("userId")
            String userId
    );
}
