package com.taskflow.user_service.service.interfaces;

import com.taskflow.user_service.dto.request.CreateUserRequest;
import com.taskflow.user_service.dto.request.UpdateUserRequest;
import com.taskflow.user_service.dto.response.UserResponse;
import com.taskflow.user_service.dto.response.UserSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(String id);

    UserResponse getUserByEmail(String email);

    UserResponse updateUser(String id,
                            UpdateUserRequest request);

    void deleteUser(String id);

    Page<UserSummaryResponse> getUsers(Pageable pageable);

}