package com.taskflow.project_service.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
// import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMapper projectMapper;
    private final UserServiceClient userServiceClient;

    @Override
    public ProjectResponse createProject(
            CreateProjectRequest request,
            String userId
    ) {

        validateUserId(userId);

        if (request == null) {
            throw new InvalidProjectException(
                    "Project creation request cannot be null"
            );
        }
        /*
         * Prevent duplicate project names for the same owner.
         *
         * This assumes ProjectRepository contains:
         *
         * boolean existsByNameIgnoreCaseAndOwnerId(
         *      String name,
         *      String ownerId
         * );
         */
        if (projectRepository.existsByNameIgnoreCaseAndOwnerId(
                request.getName(),
                userId
        )) {
            throw new DuplicateProjectException(
                    "A project with this name already exists"
            );
        }

        Project project = projectMapper.toEntity(request);

        project.setOwnerId(userId);

        Project savedProject = projectRepository.save(
                Objects.requireNonNull(project, "Project cannot be null")
        );

        /*
         * The creator automatically becomes the project owner.
         */
        ProjectMember owner = ProjectMember.builder()
                .projectId(savedProject.getId())
                .userId(userId)
                .role(ProjectRole.OWNER)
                .build();

        projectMemberRepository.save(
                Objects.requireNonNull(owner, "Project owner membership cannot be null")
        );

        return projectMapper.toResponse(savedProject);
    }



        @Override
        @Transactional(readOnly = true)
        public boolean projectExists(String projectId) {

        validateProjectId(projectId);

        return projectRepository.existsById(projectId);
        }

        @Override
        @Transactional(readOnly = true)
        public boolean isProjectMember(
                String projectId,
                String userId
        ) {

        validateProjectId(projectId);
        validateUserId(userId);

        return projectMemberRepository
                .existsByProjectIdAndUserId(
                        projectId,
                        userId
                );
        }

        @Override
        @Transactional(readOnly = true)
        public ProjectAccessResponse getProjectAccess(
                String projectId,
                String userId
        ) {

        validateProjectId(projectId);
        validateUserId(userId);

        findProject(projectId);

        ProjectMember member =
                projectMemberRepository
                        .findByProjectIdAndUserId(
                                projectId,
                                userId
                        )
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


    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(
            String projectId,
            String userId
    ) {

        validateProjectId(projectId);
        validateUserId(userId);

        Project project = findProject(projectId);

        ensureProjectAccess(projectId, userId);

        return projectMapper.toResponse(project);
    }

    @Override
    public ProjectResponse updateProject(
            String projectId,
            UpdateProjectRequest request,
            String userId
    ) {

        validateProjectId(projectId);
        validateUserId(userId);

        if (request == null) {
            throw new InvalidProjectException(
                    "Project update request cannot be null"
            );
        }

        Project project = findProject(projectId);

        ensureProjectAdminAccess(projectId, userId);

        /*
         * Only check duplicate name if the name is actually being changed.
         */
        if (request.getName() != null
                && !request.getName().equalsIgnoreCase(project.getName())
                && projectRepository.existsByNameIgnoreCaseAndOwnerId(
                        request.getName(),
                        project.getOwnerId()
                )) {

            throw new DuplicateProjectException(
                    "A project with this name already exists"
            );
        }

        projectMapper.updateEntity(request, project);

        Project updatedProject = projectRepository.save(
                Objects.requireNonNull(project, "Project cannot be null")
        );

        return projectMapper.toResponse(updatedProject);
    }

    @Override
    public void deleteProject(
            String projectId,
            String userId
    ) {

        validateProjectId(projectId);
        validateUserId(userId);

        Project project = findProject(projectId);

        /*
         * Only the owner should be allowed to delete
         * the entire project.
         */
        if (!project.getOwnerId().equals(userId)) {
            throw new ProjectAccessDeniedException(
                    "Only the project owner can delete the project"
            );
        }

        projectMemberRepository.deleteByProjectId(projectId);

        projectRepository.delete(project);
    }

        @Override
        @Transactional(readOnly = true)
        public PageResponse<ProjectSummaryResponse> getProjects(
                String userId,
                Pageable pageable
        ) {

        validateUserId(userId);

        List<String> projectIds =
                projectMemberRepository
                        .findByUserId(userId)
                        .stream()
                        .map(ProjectMember::getProjectId)
                        .distinct()
                        .toList();

        Page<Project> projects;

        if (projectIds.isEmpty()) {
                projects = Page.empty(pageable);
        } else {
                projects = projectRepository.findByIdIn(
                        projectIds,
                        pageable
                );
        }

        return PageResponse.from(
                projects.map(project -> {

                        long memberCount =
                                projectMemberRepository
                                        .countByProjectId(
                                                project.getId()
                                        );

                        return projectMapper.toSummaryResponse(
                                project,
                                memberCount
                        );
                })
        );
        }

        @Override
        @Transactional(readOnly = true)
        public PageResponse<ProjectSummaryResponse> searchProjects(
                String userId,
                String search,
                Pageable pageable
        ) {

        validateUserId(userId);

        if (search == null || search.isBlank()) {
                return getProjects(userId, pageable);
        }

        List<String> projectIds =
                projectMemberRepository
                        .findByUserId(userId)
                        .stream()
                        .map(ProjectMember::getProjectId)
                        .distinct()
                        .toList();

        Page<Project> projects;

        if (projectIds.isEmpty()) {
                projects = Page.empty(pageable);
        } else {
                projects =
                        projectRepository
                                .findByIdInAndNameContainingIgnoreCase(
                                        projectIds,
                                        search.trim(),
                                        pageable
                                );
        }

        return PageResponse.from(
                projects.map(project -> {

                        long memberCount =
                                projectMemberRepository
                                        .countByProjectId(
                                                project.getId()
                                        );

                        return projectMapper.toSummaryResponse(
                                project,
                                memberCount
                        );
                })
        );
        }


@Override
public ProjectMemberResponse addProjectMember(
                String projectId,
                AddProjectMemberRequest request,
                String userId
        ) {

        validateProjectId(projectId);
        validateUserId(userId);

        if (request == null) {
                throw new InvalidProjectException(
                        "Add member request cannot be null"
                );
        }

        validateUserId(request.getUserId());

        // Make sure the project actually exists.
        findProject(projectId);

        // Only OWNER / ADMIN can add project members.
        ensureProjectAdminAccess(projectId, userId);

        // Make sure the target user exists in user-service.
        if (!userServiceClient.userExists(request.getUserId())) {
                throw new InvalidProjectException(
                        "User does not exist"
                );
        }

        // Prevent duplicate memberships.
        if (projectMemberRepository
                .existsByProjectIdAndUserId(
                        projectId,
                        request.getUserId()
                )) {

                throw new InvalidProjectException(
                        "User is already a member of this project"
                );
        }

        ProjectMember member =
                projectMapper.toMemberEntity(
                        request,
                        projectId
                );

        ProjectMember savedMember =
                projectMemberRepository.save(
                        Objects.requireNonNull(
                                member,
                                "Project member cannot be null"
                        )
                );

        return projectMapper.toMemberResponse(savedMember);
}


    @Override
    public ProjectMemberResponse updateProjectMember(
            String projectId,
            String memberId,
            UpdateProjectMemberRequest request,
            String userId
    ) {

        validateProjectId(projectId);
        validateUserId(userId);

        if (request == null) {
            throw new InvalidProjectException(
                    "Update member request cannot be null"
            );
        }

        ensureProjectAdminAccess(projectId, userId);

        ProjectMember member =
                projectMemberRepository
                        .findById(memberId)
                        .orElseThrow(() ->
                                new InvalidProjectException(
                                        "Project member not found"
                                )
                        );

        if (!projectId.equals(member.getProjectId())) {
            throw new InvalidProjectException(
                    "Project member does not belong to this project"
            );
        }

        /*
         * Do not allow changing the owner through the
         * generic member update operation.
         */
        if (member.getRole() == ProjectRole.OWNER) {
            throw new InvalidProjectException(
                    "The project owner role cannot be changed here"
            );
        }

        projectMapper.updateMemberEntity(member, request);

        ProjectMember updatedMember =
                projectMemberRepository.save(member);

        return projectMapper.toMemberResponse(updatedMember);
    }

    @Override
    public void removeProjectMember(
            String projectId,
            String memberId,
            String userId
    ) {

        validateProjectId(projectId);
        validateUserId(userId);

        ensureProjectAdminAccess(projectId, userId);

        ProjectMember member =
                projectMemberRepository
                        .findById(memberId)
                        .orElseThrow(() ->
                                new InvalidProjectException(
                                        "Project member not found"
                                )
                        );

        if (!projectId.equals(member.getProjectId())) {
            throw new InvalidProjectException(
                    "Project member does not belong to this project"
            );
        }

        if (member.getRole() == ProjectRole.OWNER) {
            throw new InvalidProjectException(
                    "The project owner cannot be removed"
            );
        }

        projectMemberRepository.delete(member);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProjectMemberResponse> getProjectMembers(
            String projectId,
            String userId,
            Pageable pageable
    ) {

        validateProjectId(projectId);
        validateUserId(userId);

        findProject(projectId);

        ensureProjectAccess(projectId, userId);

        Page<ProjectMember> members =
                projectMemberRepository.findByProjectId(
                        projectId,
                        pageable
                );

        return PageResponse.from(
                members.map(projectMapper::toMemberResponse)
        );
    }

    /*
     * ============================================================
     * PRIVATE METHODS
     * ============================================================
     */

    private Project findProject(String projectId) {

        String projectIdValue = Objects.requireNonNull(projectId, "Project ID cannot be null");

        return projectRepository
                .findById(projectIdValue)
                .orElseThrow(() ->
                        new ProjectNotFoundException(
                                "Project not found: " + projectIdValue
                        )
                );
    }

    private void ensureProjectAccess(
            String projectId,
            String userId
    ) {

        if (!projectMemberRepository
                .existsByProjectIdAndUserId(projectId, userId)) {

            throw new ProjectAccessDeniedException(
                    "You do not have access to this project"
            );
        }
    }

    private void ensureProjectAdminAccess(
            String projectId,
            String userId
    ) {

        ProjectMember member =
                projectMemberRepository
                        .findByProjectIdAndUserId(
                                projectId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ProjectAccessDeniedException(
                                        "You are not a member of this project"
                                )
                        );

        if (member.getRole() != ProjectRole.OWNER
                && member.getRole() != ProjectRole.ADMIN) {

            throw new ProjectAccessDeniedException(
                    "You do not have permission to modify this project"
            );
        }
    }



    private void ensureProjectEditAccess(
        String projectId,
        String userId
        ) {

                ProjectMember member =
                        projectMemberRepository
                                .findByProjectIdAndUserId(
                                        projectId,
                                        userId
                                )
                                .orElseThrow(() ->
                                        new ProjectAccessDeniedException(
                                                "You are not a member of this project"
                                        )
                                );

                ProjectRole role = member.getRole();

                if (role != ProjectRole.OWNER
                        && role != ProjectRole.ADMIN
                        && role != ProjectRole.MANAGER) {

                        throw new ProjectAccessDeniedException(
                                "You do not have permission to modify this project"
                        );
                }
        }

    private void validateProjectId(String projectId) {

        if (projectId == null || projectId.isBlank()) {
            throw new InvalidProjectException(
                    "Project ID cannot be empty"
            );
        }
    }

    private void validateUserId(String userId) {

        if (userId == null || userId.isBlank()) {
            throw new InvalidProjectException(
                    "User ID cannot be empty"
            );
        }
    }
}