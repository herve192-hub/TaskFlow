package com.taskflow.user_service.controller;

import com.taskflow.user_service.service.interfaces.UserService;
import com.taskflow.user_service.dto.response.UserSummaryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserDirectoryControllerTest {
    private final UserService service = mock(UserService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new UserController(service))
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()).build();

    @Test
    void searchBindsQueryAndPagination() throws Exception {
        when(service.searchUsers(eq("Jane Doe"), any())).thenReturn(Page.empty(PageRequest.of(2, 20)));
        mvc.perform(get("/api/v1/users/search").param("q", "Jane Doe").param("page", "2").param("size", "20"))
                .andExpect(status().isOk()).andExpect(jsonPath("content").isArray());
        verify(service).searchUsers(eq("Jane Doe"), argThat(page -> page.getPageNumber() == 2 && page.getPageSize() == 20));
    }

    @Test
    void lookupBindsACommaSeparatedBatchOfAuthIds() throws Exception {
        when(service.lookupUsers(List.of("auth-1", "auth-2"))).thenReturn(List.of(
                UserSummaryResponse.builder().authUserId("auth-1")
                        .firstName("Jane").lastName("Doe").fullName("Jane Doe").build()
        ));
        mvc.perform(get("/api/v1/users/lookup").param("authUserIds", "auth-1,auth-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Jane"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"));
        verify(service).lookupUsers(List.of("auth-1", "auth-2"));
    }
}
