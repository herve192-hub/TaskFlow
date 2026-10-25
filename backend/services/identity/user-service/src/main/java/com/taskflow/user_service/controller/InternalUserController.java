package com.taskflow.user_service.controller;

import com.taskflow.user_service.dto.response.InternalUserResponse;
import com.taskflow.user_service.service.interfaces.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authenticated internal lookup API for project/task Feign clients using canonical auth IDs.
 * Returns a bounded internal summary or an existence boolean; does not expose password state.
 * The gateway omits /internal routes, but direct callers still pass the service security chain.
 * Existence means a profile is present, not necessarily active or authorized for a project.
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserService userService;

    @GetMapping("/{authUserId}")
    public ResponseEntity<InternalUserResponse> getUser(
            @PathVariable("authUserId") String authUserId
    ) {

        return ResponseEntity.ok(
                userService.getInternalUser(
                        authUserId
                )
        );
    }

    @GetMapping("/{authUserId}/exists")
    public ResponseEntity<Boolean> exists(
            @PathVariable("authUserId") String authUserId
    ) {

        return ResponseEntity.ok(
                userService.existsByAuthUserId(
                        authUserId
                )
        );
    }
}
