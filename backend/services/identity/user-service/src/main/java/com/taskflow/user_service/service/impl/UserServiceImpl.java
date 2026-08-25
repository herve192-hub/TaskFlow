package com.taskflow.user_service.service.impl;

import com.taskflow.user_service.domain.User;
import com.taskflow.user_service.domain.enums.UserStatus;

import com.taskflow.user_service.dto.request.CreateUserRequest;
import com.taskflow.user_service.dto.request.UpdateAvatarRequest;
import com.taskflow.user_service.dto.request.UpdateUserRequest;

import com.taskflow.user_service.dto.response.InternalUserResponse;
import com.taskflow.user_service.dto.response.UserResponse;
import com.taskflow.user_service.dto.response.UserSummaryResponse;

import com.taskflow.user_service.exception.DuplicateEmailException;
import com.taskflow.user_service.exception.InvalidUserException;
import com.taskflow.user_service.exception.UserNotFoundException;

import com.taskflow.user_service.mapper.UserMapper;
import com.taskflow.user_service.repository.UserRepository;
import com.taskflow.user_service.service.interfaces.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;

    private final UserMapper mapper;

    @Override
    public UserResponse createUser(
            CreateUserRequest request,
            String authUserId
    ) {

        validateAuthUserId(authUserId);

        if (repository.existsByAuthUserId(authUserId)) {
            throw new InvalidUserException(
                    "A TaskFlow profile already exists for this account"
            );
        }

        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(
                    request.getEmail()
            );
        }

        User user = mapper.toEntity(request);

        user.setAuthUserId(authUserId);
        user.setStatus(UserStatus.ACTIVE);

        return mapper.toResponse(
                repository.save(user)
        );
    }

    @Override
    public UserResponse getUserById(
            String id
    ) {

        return mapper.toResponse(
                findById(id)
        );
    }

    @Override
    public UserResponse getUserByAuthUserId(
            String authUserId
    ) {

        return mapper.toResponse(
                findByAuthUserId(authUserId)
        );
    }

    @Override
    public InternalUserResponse getInternalUser(
            String authUserId
    ) {

        return mapper.toInternalResponse(
                findByAuthUserId(authUserId)
        );
    }

    @Override
    public UserResponse getUserByEmail(
            String email
    ) {

        User user = repository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(email)
                );

        return mapper.toResponse(user);
    }

    @Override
    public UserResponse updateCurrentUser(
            String authUserId,
            UpdateUserRequest request
    ) {

        User user =
                findByAuthUserId(authUserId);

        mapper.updateEntity(
                request,
                user
        );

        return mapper.toResponse(
                repository.save(user)
        );
    }

    @Override
    public UserResponse updateAvatar(
            String authUserId,
            UpdateAvatarRequest request
    ) {

        User user =
                findByAuthUserId(authUserId);

        user.setAvatarUrl(
                request.getAvatarUrl()
        );

        return mapper.toResponse(
                repository.save(user)
        );
    }

    @Override
    public boolean existsByAuthUserId(
            String authUserId
    ) {

        if (authUserId == null ||
                authUserId.isBlank()) {

            return false;
        }

        return repository
                .existsByAuthUserId(authUserId);
    }

    @Override
    public void deactivateCurrentUser(
            String authUserId
    ) {

        User user =
                findByAuthUserId(authUserId);

        user.setStatus(
                UserStatus.INACTIVE
        );

        repository.save(user);
    }

    @Override
    public Page<UserSummaryResponse> getUsers(
            Pageable pageable
    ) {

        return repository
                .findAll(pageable)
                .map(mapper::toSummary);
    }

    private User findById(String id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(id)
                );
    }

    private User findByAuthUserId(
            String authUserId
    ) {

        validateAuthUserId(authUserId);

        return repository
                .findByAuthUserId(authUserId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                authUserId
                        )
                );
    }

    private void validateAuthUserId(
            String authUserId
    ) {

        if (authUserId == null ||
                authUserId.isBlank()) {

            throw new InvalidUserException(
                    "Authenticated user ID is required"
            );
        }
    }
}