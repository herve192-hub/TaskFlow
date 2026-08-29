package com.taskflow.project_service.service.interfaces;

import com.taskflow.project_service.dto.request.AddProjectMemberRequest;
import com.taskflow.project_service.dto.request.CreateProjectRequest;
import com.taskflow.project_service.dto.request.UpdateProjectMemberRequest;
import com.taskflow.project_service.dto.request.UpdateProjectRequest;
import com.taskflow.project_service.dto.response.PageResponse;
import com.taskflow.project_service.dto.response.ProjectAccessResponse;
import com.taskflow.project_service.dto.response.ProjectMemberResponse;
import com.taskflow.project_service.dto.response.ProjectResponse;
import com.taskflow.project_service.dto.response.ProjectSummaryResponse;
import org.springframework.data.domain.Pageable;

public interface ProjectService {

    ProjectResponse createProject(
            CreateProjectRequest request,
            String userId
    );

    ProjectResponse getProjectById(
            String projectId,
            String userId
    );

    ProjectResponse updateProject(
            String projectId,
            UpdateProjectRequest request,
            String userId
    );

    void deleteProject(
            String projectId,
            String userId
    );

    PageResponse<ProjectSummaryResponse> getProjects(
            String userId,
            Pageable pageable
    );

    PageResponse<ProjectSummaryResponse> searchProjects(
            String userId,
            String search,
            Pageable pageable
    );

    ProjectMemberResponse addProjectMember(
            String projectId,
            AddProjectMemberRequest request,
            String userId
    );

    ProjectMemberResponse updateProjectMember(
            String projectId,
            String memberId,
            UpdateProjectMemberRequest request,
            String userId
    );

    void removeProjectMember(
            String projectId,
            String memberId,
            String userId
    );

    PageResponse<ProjectMemberResponse> getProjectMembers(
            String projectId,
            String userId,
            Pageable pageable
    );

    boolean projectExists(String projectId);

    boolean isProjectMember(String projectId, String userId);

    ProjectAccessResponse getProjectAccess(String projectId, String userId);
}