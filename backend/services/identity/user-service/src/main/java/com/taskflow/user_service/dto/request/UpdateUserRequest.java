package com.taskflow.user_service.dto.request;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Editable profile fields for PUT /users/me; omitted values are kept by the null-skipping mapper.
 * Names reject whitespace-only input and lengths are bounded. Email, identity, preferences,
 * avatar, status, and timestamps have separate ownership/endpoints and are not writable here.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(max = 100)
    @Pattern(regexp = "(?s).*\\S.*", message = "First name cannot be blank")
    private String firstName;

    @Size(max = 100)
    @Pattern(regexp = "(?s).*\\S.*", message = "Last name cannot be blank")
    private String lastName;

    @Size(max = 30)
    private String phone;

    @Size(max = 500)
    private String bio;
}
