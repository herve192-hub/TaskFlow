package com.taskflow.project_service.controller;

import com.taskflow.project_service.dto.response.ProjectAccessResponse;
import com.taskflow.project_service.service.interfaces.ProjectService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

/**
 * Authenticated internal project/existence/membership API used by task-service Feign calls.
 * Access decisions derive the acting user from Authentication, never from a caller-supplied
 * identity parameter. The gateway does not publish /internal routes; the service still validates
 * credentials and reports current membership rather than trusting old token role claims.
 */
@RestController
@RequestMapping("/internal/projects")
@RequiredArgsConstructor
public class InternalProjectController {

    private final ProjectService projectService;

    @GetMapping("/{projectId}/exists")
    public ResponseEntity<Boolean> exists(
            @PathVariable("projectId") String projectId
    ) {

        return ResponseEntity.ok(
                projectService.projectExists(
                        projectId
                )
        );
    }

    /** The caller cannot nominate another identity when requesting its own project permissions. */
    @GetMapping("/{projectId}/access")
    public ResponseEntity<ProjectAccessResponse> access(
            @PathVariable("projectId") String projectId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                projectService.getProjectAccess(
                        projectId,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{projectId}/members/{userId}/exists")
    public ResponseEntity<Boolean> memberExists(
            @PathVariable("projectId") String projectId,
            @PathVariable("userId") String userId
    ) {

        return ResponseEntity.ok(
                projectService.isProjectMember(
                        projectId,
                        userId
                )
        );
    }
}
