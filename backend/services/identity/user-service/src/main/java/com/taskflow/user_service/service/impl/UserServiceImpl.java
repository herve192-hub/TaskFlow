package com.taskflow.user_service.service.impl;

import com.taskflow.user_service.domain.User;
import com.taskflow.user_service.domain.enums.UserStatus;
import com.taskflow.user_service.dto.request.CreateUserRequest;
import com.taskflow.user_service.dto.request.UpdateUserRequest;
import com.taskflow.user_service.dto.response.UserResponse;
import com.taskflow.user_service.dto.response.UserSummaryResponse;
import com.taskflow.user_service.exception.DuplicateEmailException;
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
    public UserResponse createUser(CreateUserRequest request) {

        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        User user = mapper.toEntity(request);

        return mapper.toResponse(repository.save(user));
    }

    @Override
    public UserResponse getUserById(String id) {

        return mapper.toResponse(

                repository.findById(id)
                        .orElseThrow(() ->
                                new UserNotFoundException(id))
        );
    }

    @Override
    public UserResponse getUserByEmail(String email) {

        return mapper.toResponse(

                repository.findByEmail(email)
                        .orElseThrow(() ->
                                new UserNotFoundException(email))
        );
    }

    @Override
    public UserResponse updateUser(String id,
                                   UpdateUserRequest request) {

        User user = repository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(id));

        mapper.updateEntity(request, user);

        return mapper.toResponse(repository.save(user));
    }

    @Override
    public void deleteUser(String id) {

        User user = repository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException(id));

        // repository.delete(user);
        user.setStatus(UserStatus.INACTIVE);
        repository.save(user);
    }

    @Override
    public Page<UserSummaryResponse> getUsers(Pageable pageable) {

        return repository.findAll(pageable)
                .map(mapper::toSummary);
    }

}