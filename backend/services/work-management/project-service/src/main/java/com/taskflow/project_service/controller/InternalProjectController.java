package com.taskflow.project_service.controller;

import com.taskflow.project_service.dto.response.ProjectAccessResponse;
import com.taskflow.project_service.service.interfaces.ProjectService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/projects")
@RequiredArgsConstructor
public class InternalProjectController {

    private final ProjectService projectService;

    @GetMapping("/{projectId}/exists")
    public ResponseEntity<Boolean> exists(
            @PathVariable String projectId
    ) {

        return ResponseEntity.ok(
                projectService.projectExists(
                        projectId
                )
        );
    }

    @GetMapping("/{projectId}/access")
    public ResponseEntity<ProjectAccessResponse> access(
            @PathVariable String projectId,
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
            @PathVariable String projectId,
            @PathVariable String userId
    ) {

        return ResponseEntity.ok(
                projectService.isProjectMember(
                        projectId,
                        userId
                )
        );
    }
}