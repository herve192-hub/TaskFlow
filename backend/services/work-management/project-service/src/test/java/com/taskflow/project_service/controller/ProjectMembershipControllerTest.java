package com.taskflow.project_service.controller;

import com.taskflow.project_service.domain.enums.ProjectRole;
import com.taskflow.project_service.dto.response.ProjectAccessResponse;
import com.taskflow.project_service.dto.response.ProjectMemberResponse;
import com.taskflow.project_service.exception.GlobalExceptionHandler;
import com.taskflow.project_service.exception.ProjectAccessDeniedException;
import com.taskflow.project_service.service.interfaces.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjectMembershipControllerTest {
    private final ProjectService service = mock(ProjectService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new ProjectController(service))
            .setControllerAdvice(new GlobalExceptionHandler()).build();
    private final UsernamePasswordAuthenticationToken principal =
            new UsernamePasswordAuthenticationToken("auth-actor", "unused", List.of());

    @Test
    void accessUsesTheAuthenticatedUserRatherThanARequestedUser() throws Exception {
        when(service.getProjectAccess("project-1", "auth-actor")).thenReturn(
                ProjectAccessResponse.builder().userId("auth-actor").canManageMembers(true).build());
        mvc.perform(get("/api/v1/projects/project-1/access").param("userId", "other-user").principal(principal))
                .andExpect(status().isOk()).andExpect(jsonPath("userId").value("auth-actor"));
        verify(service).getProjectAccess("project-1", "auth-actor");
    }

    @Test
    void updateBindsMembershipIdRoleAndAuthenticatedActor() throws Exception {
        when(service.updateProjectMember(eq("project-1"), eq("membership-2"), any(), eq("auth-actor")))
                .thenReturn(ProjectMemberResponse.builder().id("membership-2").role(ProjectRole.MANAGER).build());
        mvc.perform(put("/api/v1/projects/project-1/members/membership-2").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"MANAGER\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("role").value("MANAGER"));
        verify(service).updateProjectMember(eq("project-1"), eq("membership-2"),
                argThat(request -> request.getRole() == ProjectRole.MANAGER), eq("auth-actor"));
    }

    @Test
    void missingRoleIsAValidationError() throws Exception {
        mvc.perform(put("/api/v1/projects/project-1/members/membership-2").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("validationErrors.role").exists());
        verifyNoInteractions(service);
    }

    @Test
    void unknownRoleReturnsBadRequest() throws Exception {
        mvc.perform(put("/api/v1/projects/project-1/members/membership-2").principal(principal)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"UNKNOWN\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mvc.perform(delete("/api/v1/projects/project-1/members/membership-2").principal(principal))
                .andExpect(status().isNoContent());
        verify(service).removeProjectMember("project-1", "membership-2", "auth-actor");
    }

    @Test
    void deniedMembershipChangesReturnForbidden() throws Exception {
        doThrow(new ProjectAccessDeniedException("Cannot manage members"))
                .when(service).removeProjectMember("project-1", "membership-2", "auth-actor");
        mvc.perform(delete("/api/v1/projects/project-1/members/membership-2").principal(principal))
                .andExpect(status().isForbidden());
    }
}
