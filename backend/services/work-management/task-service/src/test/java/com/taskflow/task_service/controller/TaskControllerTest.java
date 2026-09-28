package com.taskflow.task_service.controller;

import com.taskflow.task_service.dto.response.PageResponse;
import com.taskflow.task_service.service.interfaces.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;

class TaskControllerTest {
    @Test
    void bindsProjectIdAndPagination() throws Exception {
        TaskService service = mock(TaskService.class);
        when(service.getTasksByProject(eq("project-123"), any(Pageable.class), eq("user-123")))
                .thenReturn(PageResponse.<com.taskflow.task_service.dto.response.TaskSummaryResponse>builder().content(List.of()).page(0).size(20).build());
        var mvc = MockMvcBuilders.standaloneSetup(new TaskController(service))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()).build();
        mvc.perform(get("/api/v1/tasks/project/project-123")
                        .param("page", "0").param("size", "20").param("sort", "createdAt,desc")
                        .principal(new UsernamePasswordAuthenticationToken("user-123", "unused", List.of())))
                .andExpect(status().isOk()).andExpect(jsonPath("content").isArray());
        verify(service).getTasksByProject(eq("project-123"), argThat(page ->
                page.getPageNumber() == 0 && page.getPageSize() == 20
                        && page.getSort().getOrderFor("createdAt").isDescending()), eq("user-123"));
    }
}
