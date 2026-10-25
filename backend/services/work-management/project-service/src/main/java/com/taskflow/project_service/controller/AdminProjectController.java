package com.taskflow.project_service.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Platform-admin route scaffold returning descriptive strings rather than persisted project operations.
 * It has role checks but is not a completed administrative CRUD API; the public gateway's
 * allowlist does not expose /api/v1/admin paths. Normal project/team behavior is in ProjectController.
 */
@RestController
@RequestMapping("/api/v1/admin/projects")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProjectController {

    @GetMapping
    public ResponseEntity<String> getAllProjects() {

        return ResponseEntity.ok(
                "Admin - Get All Projects"
        );
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<String> getProject(
            @PathVariable("projectId") String projectId) {

        return ResponseEntity.ok(
                "Admin - Get Project: " + projectId
        );
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(
            @PathVariable("projectId") String projectId) {

        return ResponseEntity.ok(
                "Admin - Delete Project: " + projectId
        );
    }
}
