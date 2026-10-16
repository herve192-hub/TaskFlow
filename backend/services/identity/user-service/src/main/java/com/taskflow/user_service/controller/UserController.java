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
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import com.taskflow.user_service.security.jwt.JwtService;
import org.springframework.security.access.AccessDeniedException;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/me")
    public ResponseEntity<UserResponse> createProfile(
            @Valid
            @RequestBody
            CreateUserRequest request,

            Authentication authentication,
            HttpServletRequest httpRequest
    ) {

        String email = jwtService.extractClaims(jwtService.extractToken(httpRequest)).get("email", String.class);
        if (!email.equalsIgnoreCase(request.getEmail().trim())) {
            throw new AccessDeniedException("Profile email must match the authenticated account");
        }
        request.setEmail(email);
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
            @PathVariable("id") String id,
            Authentication authentication
    ) {

        UserResponse profile = userService.getUserById(id);
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!admin && !authentication.getName().equals(profile.getAuthUserId())) {
            throw new AccessDeniedException("You cannot view another user's private profile");
        }
        return ResponseEntity.ok(profile);
    }

    @GetMapping
    public ResponseEntity<Page<UserSummaryResponse>> getUsers(
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                userService.getUsers(pageable)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserSummaryResponse>> searchUsers(
            @RequestParam(name = "q", defaultValue = "") String query,
            Pageable pageable
    ) {
        return ResponseEntity.ok(userService.searchUsers(query, pageable));
    }

    @GetMapping("/lookup")
    public ResponseEntity<List<UserSummaryResponse>> lookupUsers(
            @RequestParam("authUserIds") List<String> authUserIds
    ) {
        return ResponseEntity.ok(userService.lookupUsers(authUserIds));
    }
}
