package com.taskflow.project_service.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/projects")
@RequiredArgsConstructor
public class AdminProjectController {

    @GetMapping
    public ResponseEntity<String> getAllProjects() {

        return ResponseEntity.ok(
                "Admin - Get All Projects"
        );
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<String> getProject(
            @PathVariable String projectId) {

        return ResponseEntity.ok(
                "Admin - Get Project: " + projectId
        );
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(
            @PathVariable String projectId) {

        return ResponseEntity.ok(
                "Admin - Delete Project: " + projectId
        );
    }
}