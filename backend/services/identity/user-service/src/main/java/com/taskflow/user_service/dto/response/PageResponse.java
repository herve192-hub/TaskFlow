package com.taskflow.user_service.dto.response;

import lombok.*;

import java.util.List;

/**
 * Legacy generic pagination envelope retained in the profile module.
 * Current UserController directory methods return Spring Data Page directly, so changes here
 * alone do not change those endpoint payloads. Consumers must follow the actual controller DTO.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;

    private int page;

    private int size;

    private long totalElements;

    private int totalPages;

    private boolean first;

    private boolean last;
}
