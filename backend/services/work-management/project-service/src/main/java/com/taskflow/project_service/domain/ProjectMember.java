package com.taskflow.project_service.domain;

import com.taskflow.project_service.domain.enums.ProjectRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_members")
public class ProjectMember {

    @Id
    private String id;

    /**
     * ID of the user from user-service.
     */
    private String userId;

    /**
     * Project this membership belongs to.
     */
    private String projectId;

    /**
     * Role of the user within the project.
     */
    private ProjectRole role;

    /**
     * When the user joined the project.
     */
    @CreatedDate
    private Instant joinedAt;
}