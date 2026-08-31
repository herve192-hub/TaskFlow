package com.taskflow.project_service.service.impl;

import com.taskflow.project_service.client.UserServiceClient;
import com.taskflow.project_service.domain.Project;
import com.taskflow.project_service.domain.ProjectMember;
import com.taskflow.project_service.domain.enums.ProjectRole;
import com.taskflow.project_service.dto.request.AddProjectMemberRequest;
import com.taskflow.project_service.dto.request.CreateProjectRequest;
import com.taskflow.project_service.dto.request.UpdateProjectMemberRequest;
import com.taskflow.project_service.dto.request.UpdateProjectRequest;
import com.taskflow.project_service.dto.response.PageResponse;
import com.taskflow.project_service.dto.response.ProjectAccessResponse;
import com.taskflow.project_service.dto.response.ProjectMemberResponse;
import com.taskflow.project_service.dto.response.ProjectResponse;
import com.taskflow.project_service.dto.response.ProjectSummaryResponse;
import com.taskflow.project_service.exception.DuplicateProjectException;
import com.taskflow.project_service.exception.InvalidProjectException;
import com.taskflow.project_service.exception.ProjectAccessDeniedException;
import com.taskflow.project_service.exception.ProjectNotFoundException;
import com.taskflow.project_service.mapper.ProjectMapper;
import com.taskflow.project_service.repository.ProjectMemberRepository;
import com.taskflow.project_service.repository.ProjectRepository;
import com.taskflow.project_service.service.interfaces.ProjectService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMapper projectMapper;
    private final UserServiceClient userServiceClient;

    // =========================================================
    // PROJECT CREATION
    // =========================================================

    @Override
    public ProjectResponse createProject( CreateProjectRequest request, String userId )
    {

        validateUserId(userId);

        if (request == null) {
            throw new InvalidProjectException( "Project creation request cannot be null" );
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new InvalidProjectException( "Project name is required" );
        }
        /*
         * A user may not own two active projects with
         * the same name.
         */
        if (projectRepository
                .existsByNameIgnoreCaseAndOwnerId( request.getName().trim(), userId)) {
            throw new DuplicateProjectException( "A project with this name already exists" );
        }

        Project project = projectMapper.toEntity(request);

        project.setOwnerId(userId);

        Project savedProject =  projectRepository.save(
                        Objects.requireNonNull( project, "Project cannot be null" )
                );
        /*
         * Every project creator automatically becomes
         * the OWNER project member.
         */
        ProjectMember owner =
                ProjectMember.builder()
                        .projectId(savedProject.getId())
                        .userId(userId)
                        .role(ProjectRole.OWNER)
                        .build();

        projectMemberRepository.save(
                Objects.requireNonNull( owner, "Project owner membership cannot be null" )
        );

        return projectMapper.toResponse( savedProject );
    }

    // =========================================================
    // INTERNAL PROJECT LOOKUPS
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public boolean projectExists( String projectId ) {

        validateProjectId(projectId);

        return projectRepository.existsById(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProjectMember( String projectId, String userId ) {

        validateProjectId(projectId);
        validateUserId(userId);

        return projectMemberRepository
                .existsByProjectIdAndUserId( projectId, userId );
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectAccessResponse getProjectAccess( String projectId, String userId ) {

        validateProjectId(projectId);
        validateUserId(userId);

        findProject(projectId);

        ProjectMember member =
                projectMemberRepository
                        .findByProjectIdAndUserId( projectId, userId )
                        .orElse(null);
        if (member == null) {

            return ProjectAccessResponse.builder()
                    .projectId(projectId)
                    .userId(userId)
                    .member(false)
                    .canView(false)
                    .canEdit(false)
                    .canManageMembers(false)
                    .build();
        }

        ProjectRole role = member.getRole();

        boolean canEdit =
                role == ProjectRole.OWNER
                        || role == ProjectRole.ADMIN
                        || role == ProjectRole.MANAGER;

        boolean canManageMembers =
                role == ProjectRole.OWNER
                        || role == ProjectRole.ADMIN;

        return ProjectAccessResponse.builder()
                .projectId(projectId)
                .userId(userId)
                .member(true)
                .role(role)
                .canView(true)
                .canEdit(canEdit)
                .canManageMembers(canManageMembers)
                .build();
    }

    // =========================================================
    // PROJECT RETRIEVAL
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById( String projectId, String userId ) {

        validateProjectId(projectId);
        validateUserId(userId);

        Project project = findProject(projectId);

        ensureProjectAccess( projectId, userId );

        return projectMapper.toResponse( project );
    }

    // =========================================================
    // PROJECT UPDATE
    // =========================================================
    @Override
    public ProjectResponse updateProject( String projectId, UpdateProjectRequest request, String userId ) {

        validateProjectId(projectId);
        validateUserId(userId);

        if (request == null) {
            throw new InvalidProjectException(  "Project update request cannot be null" );
        }

        Project project = findProject(projectId);
        /*
         * OWNER, ADMIN and MANAGER may edit
         * project details.
         */
        ensureProjectEditAccess( projectId, userId );

        if (request.getName() != null
                && !request.getName().isBlank()
                && !request.getName()
                        .equalsIgnoreCase(
                                project.getName()
                        )
                && projectRepository
                        .existsByNameIgnoreCaseAndOwnerId(
                                request.getName().trim(),
                                project.getOwnerId()
                        )) {

            throw new DuplicateProjectException(
                    "A project with this name already exists"
            );
        }

        projectMapper.updateEntity( request, project );

        Project updatedProject = projectRepository.save(project);

        return projectMapper.toResponse( updatedProject );
    }

    // =========================================================
    // PROJECT DELETE
    // =========================================================
    @Override
    public void deleteProject( String projectId, String userId ) {

        validateProjectId(projectId);
        validateUserId(userId);

        Project project = findProject(projectId);

        /*
         * Only OWNER may delete the entire project.
         */
        if (!Objects.equals( project.getOwnerId(), userId )) {
            throw new ProjectAccessDeniedException(  "Only the project owner can delete the project" );
        }

        /*
         * Remove project memberships first.
         */
        projectMemberRepository
                .deleteByProjectId(projectId);
        projectRepository.delete(project);
    }

    // =========================================================
    // USER PROJECT LIST
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectSummaryResponse> getProjects( String userId, Pageable pageable ) {

        validateUserId(userId);

        List<String> projectIds =
                projectMemberRepository
                        .findByUserId(userId)
                        .stream()
                        .map(ProjectMember::getProjectId)
                        .distinct()
                        .toList();

        Page<Project> projects =
                projectIds.isEmpty()
                        ? Page.empty(pageable)
                        : projectRepository.findByIdIn( projectIds, pageable );

        Page<ProjectSummaryResponse> response =
                projects.map(project -> {

                    long memberCount =
                            projectMemberRepository
                                    .countByProjectId(
                                            project.getId()
                                    );

                    return projectMapper
                            .toSummaryResponse( project, memberCount );
                });

        return PageResponse.from(response);
    }

    // =========================================================
    // PROJECT SEARCH
    // =========================================================
    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectSummaryResponse> searchProjects( String userId, String search, Pageable pageable ) {

        validateUserId(userId);

        if (search == null || search.isBlank()) {

            return getProjects( userId, pageable );
        }

        List<String> projectIds =
                projectMemberRepository
                        .findByUserId(userId)
                        .stream()
                        .map(ProjectMember::getProjectId)
                        .distinct()
                        .toList();

        Page<Project> projects =
                projectIds.isEmpty()
                        ? Page.empty(pageable)
                        : projectRepository
                                .findByIdInAndNameContainingIgnoreCase(
                                        projectIds, search.trim(), pageable
                                );

        Page<ProjectSummaryResponse> response =
                projects.map(project -> {

                    long memberCount =
                            projectMemberRepository
                                    .countByProjectId(
                                            project.getId()
                                    );

                    return projectMapper
                            .toSummaryResponse( project, memberCount );
                });

        return PageResponse.from(response);
    }

    // =========================================================
    // PROJECT MEMBERS
    // =========================================================
    @Override
    public ProjectMemberResponse addProjectMember( String projectId, AddProjectMemberRequest request, String userId ) {

        validateProjectId(projectId);
        validateUserId(userId);

        if (request == null) {
            throw new InvalidProjectException( "Add member request cannot be null" );
        }

        validateUserId(
                request.getUserId()
        );

        findProject(projectId);
        /*
         * Only OWNER / ADMIN can manage membership.
         */
        ensureProjectAdminAccess( projectId, userId );
        /*
         * User must exist in user-service.
         */
        if (!userServiceClient
                .userExists(
                        request.getUserId()
                )) {

            throw new InvalidProjectException( "User does not exist" );
        }

        if (projectMemberRepository
                .existsByProjectIdAndUserId(
                        projectId,
                        request.getUserId()
                )) {

            throw new InvalidProjectException( "User is already a member of this project" );
        }

        /*
         * OWNER must never be assigned through this endpoint.
         * Ownership is established only during project creation.
         */
        if (request.getRole()
                == ProjectRole.OWNER) {
            throw new InvalidProjectException( "OWNER role cannot be assigned manually" );
        }

        ProjectMember member = projectMapper.toMemberEntity( request, projectId );

        ProjectMember savedMember =
                projectMemberRepository.save(
                        Objects.requireNonNull(
                                member, "Project member cannot be null" )
                );

        return projectMapper.toMemberResponse( savedMember );
    }

    @Override
    public ProjectMemberResponse updateProjectMember( String projectId, String memberId,
            UpdateProjectMemberRequest request, String userId ) {

        validateProjectId(projectId);
        validateUserId(userId);
        validateMemberId(memberId);

        if (request == null) {
            throw new InvalidProjectException(  "Update member request cannot be null" );
        }

        ensureProjectAdminAccess( projectId, userId );
        ProjectMember member = findProjectMember(memberId);
        ensureMemberBelongsToProject( member, projectId );
        /*
         * OWNER cannot be modified through the generic
         * membership endpoint.
         */
        if (member.getRole() == ProjectRole.OWNER) {

            throw new InvalidProjectException( "The project owner role cannot be changed here" );
        }

        /*
         * Prevent another member from being promoted to OWNER.
         */
        if (request.getRole() == ProjectRole.OWNER) {

            throw new InvalidProjectException( "OWNER role cannot be assigned manually" );
        }

        projectMapper.updateMemberEntity( member, request );

        ProjectMember updatedMember = projectMemberRepository.save(member);

        return projectMapper.toMemberResponse( updatedMember );
    }

    @Override
    public void removeProjectMember( String projectId, String memberId, String userId ) {

        validateProjectId(projectId);
        validateUserId(userId);
        validateMemberId(memberId);

        ensureProjectAdminAccess( projectId, userId );

        ProjectMember member = findProjectMember(memberId);

        ensureMemberBelongsToProject( member,  projectId
        );

        if (member.getRole() == ProjectRole.OWNER) {

            throw new InvalidProjectException(  "The project owner cannot be removed" );
        }

        projectMemberRepository.delete(member);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectMemberResponse> getProjectMembers(  String projectId, String userId, Pageable pageable ) {

        validateProjectId(projectId);
        validateUserId(userId);

        findProject(projectId);

        ensureProjectAccess( projectId, userId );

        Page<ProjectMemberResponse> members =
                projectMemberRepository
                        .findByProjectId(
                                projectId, pageable )
                        .map(
                                projectMapper::toMemberResponse
                        );
        return PageResponse.from(members);
    }

    // =========================================================
    // LOOKUP HELPERS
    // =========================================================
    private Project findProject( String projectId ) {

        validateProjectId(projectId);

        return projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new ProjectNotFoundException(
                                "Project not found: "
                                        + projectId
                        )
                );
    }

    private ProjectMember findProjectMember( String memberId ) {

        validateMemberId(memberId);

        return projectMemberRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new InvalidProjectException( "Project member not found" ) );
    }

    // =========================================================
    // ACCESS CONTROL
    // =========================================================
    private void ensureProjectAccess(  String projectId, String userId ) {

        if (!projectMemberRepository
                .existsByProjectIdAndUserId( projectId, userId )) {

            throw new ProjectAccessDeniedException( "You do not have access to this project" );
        }
    }

    /**
     * OWNER / ADMIN may manage project membership.
     */
    private void ensureProjectAdminAccess( String projectId, String userId ) {

        ProjectMember member =
                getMembership( projectId, userId );

        ProjectRole role = member.getRole();

        if (role != ProjectRole.OWNER
                && role != ProjectRole.ADMIN) {

            throw new ProjectAccessDeniedException(
                    "You do not have permission to manage project members"
            );
        }
    }

    /**
     * OWNER / ADMIN / MANAGER may edit project details.
     */
    private void ensureProjectEditAccess( String projectId, String userId ) {

        ProjectMember member = getMembership( projectId, userId );

        ProjectRole role = member.getRole();

        if (role != ProjectRole.OWNER
                && role != ProjectRole.ADMIN
                && role != ProjectRole.MANAGER) {

            throw new ProjectAccessDeniedException(
                    "You do not have permission to modify this project"
            );
        }
    }

    private ProjectMember getMembership( String projectId, String userId ) {

        return projectMemberRepository
                .findByProjectIdAndUserId( projectId, userId )
                .orElseThrow(() ->
                        new ProjectAccessDeniedException(
                                "You are not a member of this project"
                        )
                );
    }

    private void ensureMemberBelongsToProject( ProjectMember member, String projectId ) {

        if (!Objects.equals( projectId, member.getProjectId() )) {

            throw new InvalidProjectException(
                    "Project member does not belong to this project"
            );
        }
    }

    // =========================================================
    // VALIDATION
    // =========================================================
    private void validateProjectId( String projectId ) {

        if (projectId == null || projectId.isBlank()) {

            throw new InvalidProjectException( "Project ID cannot be empty" );
        }
    }

    private void validateUserId( String userId ) {

        if (userId == null || userId.isBlank()) {

            throw new InvalidProjectException( "User ID cannot be empty"  );
        }
    }

    private void validateMemberId( String memberId ) {

        if (memberId == null || memberId.isBlank()) {
            throw new InvalidProjectException( "Project member ID cannot be empty" );
        }
    }
}