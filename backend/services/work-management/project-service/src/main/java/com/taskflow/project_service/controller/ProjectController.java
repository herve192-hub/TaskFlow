package com.taskflow.project_service.controller;

import com.taskflow.project_service.dto.request.*;
import com.taskflow.project_service.dto.response.*;
import com.taskflow.project_service.service.interfaces.ProjectService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid
            @RequestBody
            CreateProjectRequest request,

            Authentication authentication
    ) {

        ProjectResponse response =
                projectService.createProject(
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable("projectId") String projectId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                projectService.getProjectById(
                        projectId,
                        authentication.getName()
                )
        );
    }

    @GetMapping
    public ResponseEntity<PageResponse<ProjectSummaryResponse>>
    getProjects(
            Pageable pageable,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                projectService.getProjects(
                        authentication.getName(),
                        pageable
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<ProjectSummaryResponse>>
    searchProjects(
            @RequestParam("q") String q,
            Pageable pageable,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                projectService.searchProjects(
                        authentication.getName(),
                        q,
                        pageable
                )
        );
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable("projectId") String projectId,

            @Valid
            @RequestBody
            UpdateProjectRequest request,

            Authentication authentication
    ) {

        return ResponseEntity.ok(
                projectService.updateProject(
                        projectId,
                        request,
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable("projectId") String projectId,
            Authentication authentication
    ) {

        projectService.deleteProject(
                projectId,
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<PageResponse<ProjectMemberResponse>>
    getMembers(
            @PathVariable("projectId") String projectId,
            Pageable pageable,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                projectService.getProjectMembers(
                        projectId,
                        authentication.getName(),
                        pageable
                )
        );
    }

    @GetMapping("/{projectId}/access")
    public ResponseEntity<ProjectAccessResponse> getAccess(
            @PathVariable("projectId") String projectId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                projectService.getProjectAccess(projectId, authentication.getName())
        );
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<ProjectMemberResponse> addMember(
            @PathVariable("projectId") String projectId,

            @Valid
            @RequestBody
            AddProjectMemberRequest request,

            Authentication authentication
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        projectService.addProjectMember(
                                projectId,
                                request,
                                authentication.getName()
                        )
                );
    }

    @PutMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<ProjectMemberResponse> updateMember(
            @PathVariable("projectId") String projectId,
            @PathVariable("memberId") String memberId,
            @Valid @RequestBody UpdateProjectMemberRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                projectService.updateProjectMember(
                        projectId, memberId, request, authentication.getName()
                )
        );
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable("projectId") String projectId,
            @PathVariable("memberId") String memberId,
            Authentication authentication
    ) {
        projectService.removeProjectMember(projectId, memberId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
