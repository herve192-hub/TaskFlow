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

/**
 * Project/team application contract shared by public controllers and internal access endpoints.
 * Callers supply canonical authenticated actor IDs; implementations enforce owner/member roles,
 * validate reference IDs, and paginate authorized results. This interface does not define task
 * cleanup or distributed operations on another service's database.
 */
public interface ProjectService {

    /** Create the project and its creator's OWNER membership. */
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

    /** Delete a project and its memberships; only the recorded owner may request deletion. */
    void deleteProject(
            String projectId,
            String userId
    );

    /** List projects through membership records, so owners and invited users share one access model. */
    PageResponse<ProjectSummaryResponse> getProjects(
            String userId,
            Pageable pageable
    );

    PageResponse<ProjectSummaryResponse> searchProjects(
            String userId,
            String search,
            Pageable pageable
    );

    /** Add an existing auth identity with a non-OWNER role; OWNER/ADMIN membership is required. */
    ProjectMemberResponse addProjectMember(
            String projectId,
            AddProjectMemberRequest request,
            String userId
    );

    /** Change a non-owner membership identified by its membership document ID. */
    ProjectMemberResponse updateProjectMember(
            String projectId,
            String memberId,
            UpdateProjectMemberRequest request,
            String userId
    );

    /** Remove a non-owner membership; memberId is not the member's auth-service user ID. */
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

    /** Return current project permissions, including a denied response for a non-member. */
    ProjectAccessResponse getProjectAccess(String projectId, String userId);
}
