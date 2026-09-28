package com.taskflow.user_service.controller;

import com.taskflow.user_service.service.interfaces.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InternalUserControllerTest {
    @Test
    void bindsUserIdForLookupAndExistenceCheck() throws Exception {
        UserService service = mock(UserService.class);
        when(service.existsByAuthUserId("user-123")).thenReturn(true);
        var mvc = MockMvcBuilders.standaloneSetup(new InternalUserController(service)).build();
        mvc.perform(get("/internal/users/user-123/exists"))
                .andExpect(status().isOk()).andExpect(content().string("true"));
        mvc.perform(get("/internal/users/user-123")).andExpect(status().isOk());
        verify(service).existsByAuthUserId("user-123");
        verify(service).getInternalUser("user-123");
    }
}
