package com.taskflow.user_service.mapper;

import com.taskflow.user_service.domain.User;
import com.taskflow.user_service.dto.request.CreateUserRequest;
import com.taskflow.user_service.dto.request.UpdateUserRequest;
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
        return mapper.map(user, UserResponse.class);
    }

    public UserSummaryResponse toSummary(User user) {
        return mapper.map(user, UserSummaryResponse.class);
    }

    public void updateEntity(UpdateUserRequest request, User user) {
        mapper.map(request, user);
    }

}