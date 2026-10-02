package com.taskflow.project_service.service;

import com.taskflow.project_service.client.UserServiceClient;
import com.taskflow.project_service.config.ModelMapperConfig;
import com.taskflow.project_service.domain.Project;
import com.taskflow.project_service.domain.ProjectMember;
import com.taskflow.project_service.domain.enums.ProjectRole;
import com.taskflow.project_service.dto.request.AddProjectMemberRequest;
import com.taskflow.project_service.dto.request.CreateProjectRequest;
import com.taskflow.project_service.dto.request.UpdateProjectMemberRequest;
import com.taskflow.project_service.exception.InvalidProjectException;
import com.taskflow.project_service.exception.ProjectAccessDeniedException;
import com.taskflow.project_service.mapper.ProjectMapper;
import com.taskflow.project_service.repository.ProjectMemberRepository;
import com.taskflow.project_service.repository.ProjectRepository;
import com.taskflow.project_service.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProjectMembershipTest {
    private final ProjectRepository projects = mock(ProjectRepository.class);
    private final ProjectMemberRepository members = mock(ProjectMemberRepository.class);
    private final UserServiceClient users = mock(UserServiceClient.class);
    private final ProjectMapper mapper = new ProjectMapper(new ModelMapperConfig().modelMapper());
    private final ProjectServiceImpl service = new ProjectServiceImpl(projects, members, mapper, users);

    private void existingProject() {
        when(projects.findById("project-1")).thenReturn(Optional.of(
                Project.builder().id("project-1").ownerId("owner-1").build()));
    }

    private void actor(ProjectRole role) {
        when(members.findByProjectIdAndUserId("project-1", "actor-1"))
                .thenReturn(Optional.of(member("actor-membership", "actor-1", role)));
    }

    private ProjectMember member(String id, String userId, ProjectRole role) {
        return ProjectMember.builder().id(id).projectId("project-1").userId(userId).role(role).build();
    }

    private AddProjectMemberRequest addRequest(ProjectRole role) {
        var request = new AddProjectMemberRequest();
        request.setUserId("auth-user-2");
        request.setRole(role);
        return request;
    }

    private UpdateProjectMemberRequest updateRequest(ProjectRole role) {
        var request = new UpdateProjectMemberRequest();
        request.setRole(role);
        return request;
    }

    @Test
    void projectCreationMakesTheAuthenticatedUserItsOwner() {
        var request = new CreateProjectRequest();
        request.setName("Team project");
        when(projects.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId("project-1");
            return project;
        });

        service.createProject(request, "auth-owner-1");

        verify(members).save(argThat(member -> member.getProjectId().equals("project-1")
                && member.getUserId().equals("auth-owner-1") && member.getRole() == ProjectRole.OWNER));
    }

    @ParameterizedTest
    @EnumSource(value = ProjectRole.class, names = {"OWNER", "ADMIN"})
    void ownersAndAdminsCanAddMembersUsingTheirAuthUserId(ProjectRole role) {
        existingProject();
        actor(role);
        when(users.userExists("auth-user-2")).thenReturn(true);
        when(members.save(any(ProjectMember.class))).thenAnswer(invocation -> {
            ProjectMember member = invocation.getArgument(0);
            member.setId("membership-2");
            return member;
        });

        var response = service.addProjectMember("project-1", addRequest(ProjectRole.MEMBER), "actor-1");

        assertThat(response.getUserId()).isEqualTo("auth-user-2");
        assertThat(response.getRole()).isEqualTo(ProjectRole.MEMBER);
        verify(members).save(argThat(member -> member.getProjectId().equals("project-1")));
    }

    @ParameterizedTest
    @EnumSource(value = ProjectRole.class, names = {"MANAGER", "MEMBER", "GUEST"})
    void otherRolesCannotAddUpdateOrRemoveMembers(ProjectRole role) {
        existingProject();
        actor(role);

        assertThatThrownBy(() -> service.addProjectMember("project-1", addRequest(ProjectRole.MEMBER), "actor-1"))
                .isInstanceOf(ProjectAccessDeniedException.class);
        assertThatThrownBy(() -> service.updateProjectMember("project-1", "membership-2", updateRequest(ProjectRole.ADMIN), "actor-1"))
                .isInstanceOf(ProjectAccessDeniedException.class);
        assertThatThrownBy(() -> service.removeProjectMember("project-1", "membership-2", "actor-1"))
                .isInstanceOf(ProjectAccessDeniedException.class);
        verifyNoInteractions(users);
        verify(members, never()).save(any());
        verify(members, never()).delete(any());
    }

    @Test
    void duplicateMembersAreRejected() {
        existingProject();
        actor(ProjectRole.OWNER);
        when(users.userExists("auth-user-2")).thenReturn(true);
        when(members.existsByProjectIdAndUserId("project-1", "auth-user-2")).thenReturn(true);

        assertThatThrownBy(() -> service.addProjectMember("project-1", addRequest(ProjectRole.MEMBER), "actor-1"))
                .isInstanceOf(InvalidProjectException.class).hasMessageContaining("already a member");
        verify(members, never()).save(any());
    }

    @Test
    void nonexistentUsersAreRejected() {
        existingProject();
        actor(ProjectRole.ADMIN);

        assertThatThrownBy(() -> service.addProjectMember("project-1", addRequest(ProjectRole.MEMBER), "actor-1"))
                .isInstanceOf(InvalidProjectException.class).hasMessage("User does not exist");
        verify(members, never()).save(any());
    }

    @Test
    void ownerCannotBeAssignedDuringAddition() {
        existingProject();
        actor(ProjectRole.OWNER);
        when(users.userExists("auth-user-2")).thenReturn(true);

        assertThatThrownBy(() -> service.addProjectMember("project-1", addRequest(ProjectRole.OWNER), "actor-1"))
                .isInstanceOf(InvalidProjectException.class).hasMessageContaining("OWNER");
        verify(members, never()).save(any());
    }

    @Test
    void ownerCannotBeDemotedOrRemoved() {
        existingProject();
        actor(ProjectRole.ADMIN);
        when(members.findById("owner-membership"))
                .thenReturn(Optional.of(member("owner-membership", "owner-1", ProjectRole.OWNER)));

        assertThatThrownBy(() -> service.updateProjectMember("project-1", "owner-membership", updateRequest(ProjectRole.MEMBER), "actor-1"))
                .isInstanceOf(InvalidProjectException.class).hasMessageContaining("owner");
        assertThatThrownBy(() -> service.removeProjectMember("project-1", "owner-membership", "actor-1"))
                .isInstanceOf(InvalidProjectException.class).hasMessageContaining("owner");
        verify(members, never()).save(any());
        verify(members, never()).delete(any());
    }

    @Test
    void memberCannotBePromotedToOwner() {
        existingProject();
        actor(ProjectRole.OWNER);
        when(members.findById("membership-2"))
                .thenReturn(Optional.of(member("membership-2", "auth-user-2", ProjectRole.MEMBER)));

        assertThatThrownBy(() -> service.updateProjectMember("project-1", "membership-2", updateRequest(ProjectRole.OWNER), "actor-1"))
                .isInstanceOf(InvalidProjectException.class).hasMessageContaining("OWNER");
        verify(members, never()).save(any());
    }

    @Test
    void membershipFromAnotherProjectCannotBeChangedOrRemoved() {
        existingProject();
        actor(ProjectRole.OWNER);
        var otherMember = member("membership-2", "auth-user-2", ProjectRole.MEMBER);
        otherMember.setProjectId("project-2");
        when(members.findById("membership-2")).thenReturn(Optional.of(otherMember));

        assertThatThrownBy(() -> service.updateProjectMember("project-1", "membership-2", updateRequest(ProjectRole.ADMIN), "actor-1"))
                .isInstanceOf(InvalidProjectException.class).hasMessageContaining("does not belong");
        assertThatThrownBy(() -> service.removeProjectMember("project-1", "membership-2", "actor-1"))
                .isInstanceOf(InvalidProjectException.class).hasMessageContaining("does not belong");
        verify(members, never()).save(any());
        verify(members, never()).delete(any());
    }

    @Test
    void adminCanChangeAndRemoveAnOrdinaryMember() {
        existingProject();
        actor(ProjectRole.ADMIN);
        var member = member("membership-2", "auth-user-2", ProjectRole.MEMBER);
        when(members.findById("membership-2")).thenReturn(Optional.of(member));
        when(members.save(any(ProjectMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.updateProjectMember("project-1", "membership-2", updateRequest(ProjectRole.MANAGER), "actor-1");
        assertThat(response.getRole()).isEqualTo(ProjectRole.MANAGER);
        service.removeProjectMember("project-1", "membership-2", "actor-1");
        verify(members).delete(member);
    }

    @ParameterizedTest
    @EnumSource(ProjectRole.class)
    void permissionsMatchTheProjectRole(ProjectRole role) {
        existingProject();
        actor(role);

        var access = service.getProjectAccess("project-1", "actor-1");

        assertThat(access.isMember()).isTrue();
        assertThat(access.isCanView()).isTrue();
        assertThat(access.isCanManageMembers()).isEqualTo(role == ProjectRole.OWNER || role == ProjectRole.ADMIN);
        assertThat(access.isCanEdit()).isEqualTo(role == ProjectRole.OWNER || role == ProjectRole.ADMIN || role == ProjectRole.MANAGER);
    }

    @Test
    void nonMembersHaveNoProjectAccessAndCannotReadTheTeam() {
        existingProject();
        var access = service.getProjectAccess("project-1", "outsider");
        assertThat(access.isMember()).isFalse();
        assertThat(access.isCanView()).isFalse();
        assertThat(access.isCanManageMembers()).isFalse();

        assertThatThrownBy(() -> service.getProjectMembers("project-1", "outsider", PageRequest.of(0, 20)))
                .isInstanceOf(ProjectAccessDeniedException.class);
        verify(members, never()).findByProjectId(anyString(), any());
    }

    @Test
    void membersCanReadAPaginatedTeam() {
        existingProject();
        when(members.existsByProjectIdAndUserId("project-1", "actor-1")).thenReturn(true);
        var pageable = PageRequest.of(1, 20);
        when(members.findByProjectId("project-1", pageable)).thenReturn(new PageImpl<>(
                List.of(member("membership-2", "auth-user-2", ProjectRole.MEMBER)), pageable, 21));

        var response = service.getProjectMembers("project-1", "actor-1", pageable);
        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getTotalElements()).isEqualTo(21);
        assertThat(response.getContent()).extracting("userId").containsExactly("auth-user-2");
    }

    @Test
    void missingRolesAreRejectedEvenForNonHttpCallers() {
        assertThatThrownBy(() -> service.addProjectMember("project-1", addRequest(null), "actor-1"))
                .isInstanceOf(InvalidProjectException.class).hasMessage("Project role is required");
        assertThatThrownBy(() -> service.updateProjectMember("project-1", "membership-2", updateRequest(null), "actor-1"))
                .isInstanceOf(InvalidProjectException.class).hasMessage("Project role is required");
        verify(members, never()).save(any());
    }
}
