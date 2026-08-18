package com.taskflow.project_service.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    @GetMapping
    public ResponseEntity<String> getProjects() {

        return ResponseEntity.ok(
                "Project Service - Get Projects"
        );
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<String> getProject(
            @PathVariable String projectId) {

        return ResponseEntity.ok(
                "Project Service - Get Project: " + projectId
        );
    }

    @PostMapping
    public ResponseEntity<String> createProject() {

        return ResponseEntity.ok(
                "Project Service - Create Project"
        );
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<String> updateProject(
            @PathVariable String projectId) {

        return ResponseEntity.ok(
                "Project Service - Update Project: " + projectId
        );
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(
            @PathVariable String projectId) {

        return ResponseEntity.ok(
                "Project Service - Delete Project: " + projectId
        );
    }
}