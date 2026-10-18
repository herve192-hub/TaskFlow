package com.taskflow.user_service.dto.request;

import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAvatarRequest {

    /** Null removes the photo. Only bounded PNG/JPEG data images are accepted by the service. */
    @Size(max = 262144, message = "Profile photo is too large")
    private String avatarUrl;
}
