package com.taskflow.user_service.controller;

import com.taskflow.user_service.dto.response.InternalUserResponse;
import com.taskflow.user_service.service.interfaces.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{authUserId}")
    public ResponseEntity<InternalUserResponse> getUser(
            @PathVariable String authUserId
    ) {

        return ResponseEntity.ok(
                userService.getInternalUser(
                        authUserId
                )
        );
    }

    @GetMapping("/{authUserId}/exists")
    public ResponseEntity<Boolean> exists(
            @PathVariable String authUserId
    ) {

        return ResponseEntity.ok(
                userService.existsByAuthUserId(
                        authUserId
                )
        );
    }
}