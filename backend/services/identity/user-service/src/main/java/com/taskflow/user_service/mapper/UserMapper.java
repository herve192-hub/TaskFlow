package com.taskflow.user_service.mapper;

import com.taskflow.user_service.domain.User;

import com.taskflow.user_service.dto.request.CreateUserRequest;
import com.taskflow.user_service.dto.request.UpdateUserRequest;

import com.taskflow.user_service.dto.response.InternalUserResponse;
import com.taskflow.user_service.dto.response.UserResponse;
import com.taskflow.user_service.dto.response.UserSummaryResponse;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final ModelMapper mapper;

    public UserMapper(ModelMapper mapper) {
        this.mapper = mapper;
    }

    public User toEntity(CreateUserRequest request) {
        return mapper.map(request, User.class);
    }

    public UserResponse toResponse(User user) {

        UserResponse response =
                mapper.map(user, UserResponse.class);

        response.setFullName(buildFullName(user));

        return response;
    }

    public UserSummaryResponse toSummary(User user) {

        UserSummaryResponse response =
                mapper.map(user, UserSummaryResponse.class);

        response.setFullName(buildFullName(user));

        return response;
    }

    public InternalUserResponse toInternalResponse(
            User user
    ) {

        return mapper.map(
                user,
                InternalUserResponse.class
        );
    }

    public void updateEntity(
            UpdateUserRequest request,
            User user
    ) {

        mapper.map(request, user);
    }

    private String buildFullName(User user) {

        String firstName =
                user.getFirstName() == null
                        ? ""
                        : user.getFirstName().trim();

        String lastName =
                user.getLastName() == null
                        ? ""
                        : user.getLastName().trim();

        return (firstName + " " + lastName).trim();
    }
}