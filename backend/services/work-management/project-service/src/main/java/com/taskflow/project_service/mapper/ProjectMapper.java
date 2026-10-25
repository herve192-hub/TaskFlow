package com.taskflow.project_service.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.taskflow.project_service.domain.Project;
import com.taskflow.project_service.domain.ProjectMember;
import com.taskflow.project_service.dto.request.AddProjectMemberRequest;
import com.taskflow.project_service.dto.request.CreateProjectRequest;
import com.taskflow.project_service.dto.request.UpdateProjectMemberRequest;
import com.taskflow.project_service.dto.request.UpdateProjectRequest;
import com.taskflow.project_service.dto.response.ProjectMemberResponse;
import com.taskflow.project_service.dto.response.ProjectResponse;
import com.taskflow.project_service.dto.response.ProjectSummaryResponse;

import lombok.RequiredArgsConstructor;

/**
 * Maps request/entity/response boundaries without making authorization decisions.
 * Partial updates copy only supplied project fields; membership creation takes an explicit
 * project ID and canonical account ID. Summary memberCount is supplied by the service, not
 * discovered by ModelMapper. Ownership and OWNER-role protections stay in ProjectServiceImpl.
 */
@Component
@RequiredArgsConstructor
public class ProjectMapper {

    private final ModelMapper modelMapper;



    public Project toEntity(CreateProjectRequest request) {
        return modelMapper.map(request, Project.class);
    }

    public void updateEntity(
            UpdateProjectRequest request,
            Project project) {

        if (request.getName() != null) {
            project.setName(request.getName());
        }

        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        if (request.getVisibility() != null) {
            project.setVisibility(request.getVisibility());
        }
    }

    public ProjectResponse toResponse(Project project) {
        return modelMapper.map(project, ProjectResponse.class);
    }

    public ProjectSummaryResponse toSummaryResponse(
            Project project,
            long memberCount
    ) {
        ProjectSummaryResponse response =
                modelMapper.map(
                        project,
                        ProjectSummaryResponse.class
                );
        response.setMemberCount(memberCount);
        return response;
    }

    public ProjectMember toMemberEntity(
            AddProjectMemberRequest request,
            String projectId) {

        return ProjectMember.builder()
                .projectId(projectId)
                .userId(request.getUserId())
                .role(request.getRole())
                .build();
    }

    public void updateMemberEntity(
            ProjectMember member,
            UpdateProjectMemberRequest request) {

        if (request.getRole() != null) {
            member.setRole(request.getRole());
        }
    }

    public ProjectMemberResponse toMemberResponse(
            ProjectMember member) {

        return modelMapper.map(
                member,
                ProjectMemberResponse.class);
    }
}
