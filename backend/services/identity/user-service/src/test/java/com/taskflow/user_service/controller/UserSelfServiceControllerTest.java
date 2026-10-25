package com.taskflow.user_service.controller;

import com.taskflow.user_service.domain.UserPreferences;
import com.taskflow.user_service.dto.response.UserResponse;
import com.taskflow.user_service.exception.GlobalExceptionHandler;
import com.taskflow.user_service.security.jwt.JwtService;
import com.taskflow.user_service.service.interfaces.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Standalone MockMvc tests proving /me operations use the authenticated account, not body-injected IDs.
 * Exercise bounded names/private details, preference enums, safe start pages, and avatar removal.
 * The mock principal bypasses JWT filtering intentionally; full-stack tests cover cookie authentication.
 */
class UserSelfServiceControllerTest {
    private final UserService service = mock(UserService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new UserController(service, mock(JwtService.class)))
            .setControllerAdvice(new GlobalExceptionHandler()).build();
    private final UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken("signed-account", null, List.of());

    @Test void profileUpdatesAlwaysTargetTheSignedAccount() throws Exception {
        when(service.updateCurrentUser(eq("signed-account"), any())).thenReturn(UserResponse.builder().firstName("Ada").build());
        mvc.perform(put("/api/v1/users/me").principal(principal).contentType("application/json")
                        .content("{\"firstName\":\"Ada\",\"lastName\":\"Lovelace\",\"authUserId\":\"someone-else\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("firstName").value("Ada"));
        verify(service).updateCurrentUser(eq("signed-account"), argThat(request -> request.getLastName().equals("Lovelace")));
    }

    @Test void rejectsBlankNamesAndOversizedPrivateDetails() throws Exception {
        for (String json : List.of("{\"firstName\":\"   \"}", "{\"lastName\":\"\"}", "{\"bio\":\"" + "x".repeat(501) + "\"}")) {
            mvc.perform(put("/api/v1/users/me").principal(principal).contentType("application/json").content(json))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }

    @Test void preferencesHaveValidatedValuesAndUseTheAuthenticatedSubject() throws Exception {
        when(service.updatePreferences(eq("signed-account"), any())).thenReturn(UserResponse.builder()
                .preferences(UserPreferences.builder().timeZone("America/Chicago").build()).build());
        mvc.perform(put("/api/v1/users/me/preferences").principal(principal).contentType("application/json")
                        .content("{\"timeZone\":\"America/Chicago\",\"dateFormat\":\"YEAR_MONTH_DAY\",\"startPage\":\"PROJECTS\",\"use24HourTime\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("preferences.timeZone").value("America/Chicago"));
        verify(service).updatePreferences(eq("signed-account"), argThat(value -> value.getStartPage() == UserPreferences.StartPage.PROJECTS));
    }

    @Test void rejectsNullEnumsAndUnsafeLandingPages() throws Exception {
        for (String json : List.of("{\"dateFormat\":null}", "{\"startPage\":\"https://outside.example.test\"}", "{\"timeZone\":\"\"}")) {
            mvc.perform(put("/api/v1/users/me/preferences").principal(principal).contentType("application/json").content(json))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(service);
    }

    @Test void aPhotoCanBeRemovedWithoutTargetingAnotherAccount() throws Exception {
        when(service.updateAvatar(eq("signed-account"), any())).thenReturn(UserResponse.builder().build());
        mvc.perform(put("/api/v1/users/me/avatar").principal(principal).contentType("application/json").content("{\"avatarUrl\":null}"))
                .andExpect(status().isOk());
        verify(service).updateAvatar(eq("signed-account"), argThat(value -> value.getAvatarUrl() == null));
    }
}
