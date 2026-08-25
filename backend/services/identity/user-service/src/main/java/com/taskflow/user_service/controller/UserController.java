package com.taskflow.user_service.controller;

import com.taskflow.user_service.dto.request.CreateUserRequest;
import com.taskflow.user_service.dto.request.UpdateAvatarRequest;
import com.taskflow.user_service.dto.request.UpdateUserRequest;

import com.taskflow.user_service.dto.response.UserResponse;
import com.taskflow.user_service.dto.response.UserSummaryResponse;

import com.taskflow.user_service.service.interfaces.UserService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/me")
    public ResponseEntity<UserResponse> createProfile(
            @Valid
            @RequestBody
            CreateUserRequest request,

            Authentication authentication
    ) {

        UserResponse response =
                userService.createUser(
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                userService.getUserByAuthUserId(
                        authentication.getName()
                )
        );
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            @Valid
            @RequestBody
            UpdateUserRequest request,

            Authentication authentication
    ) {

        return ResponseEntity.ok(
                userService.updateCurrentUser(
                        authentication.getName(),
                        request
                )
        );
    }

    @PutMapping("/me/avatar")
    public ResponseEntity<UserResponse> updateAvatar(
            @Valid
            @RequestBody
            UpdateAvatarRequest request,

            Authentication authentication
    ) {

        return ResponseEntity.ok(
                userService.updateAvatar(
                        authentication.getName(),
                        request
                )
        );
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deactivateProfile(
            Authentication authentication
    ) {

        userService.deactivateCurrentUser(
                authentication.getName()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable String id
    ) {

        return ResponseEntity.ok(
                userService.getUserById(id)
        );
    }

    @GetMapping
    public ResponseEntity<Page<UserSummaryResponse>> getUsers(
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                userService.getUsers(pageable)
        );
    }
}