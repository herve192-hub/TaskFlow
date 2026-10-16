package com.taskflow.user_service.service.interfaces;

import com.taskflow.user_service.dto.request.CreateUserRequest;
import com.taskflow.user_service.dto.request.UpdateAvatarRequest;
import com.taskflow.user_service.dto.request.UpdateUserRequest;

import com.taskflow.user_service.dto.response.InternalUserResponse;
import com.taskflow.user_service.dto.response.UserResponse;
import com.taskflow.user_service.dto.response.UserSummaryResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/** Profile operations; authUserId is the shared account identity and id is a local profile ID. */
public interface UserService {

    UserResponse createUser(
            CreateUserRequest request,
            String authUserId
    );

    UserResponse getUserById(
            String id
    );

    UserResponse getUserByAuthUserId(
            String authUserId
    );

    InternalUserResponse getInternalUser(
            String authUserId
    );

    UserResponse getUserByEmail(
            String email
    );

    UserResponse updateCurrentUser(
            String authUserId,
            UpdateUserRequest request
    );

    UserResponse updateAvatar(
            String authUserId,
            UpdateAvatarRequest request
    );

    boolean existsByAuthUserId(
            String authUserId
    );

    /** Mark the profile inactive without changing auth-service account credentials or existing tokens. */
    void deactivateCurrentUser(
            String authUserId
    );

    Page<UserSummaryResponse> getUsers(
            Pageable pageable
    );

    Page<UserSummaryResponse> searchUsers(String query, Pageable pageable);

    List<UserSummaryResponse> lookupUsers(List<String> authUserIds);
}
