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

/**
 * Membership document linking one project ID to a canonical auth user ID and project role.
 * Its own Mongo ID is the memberId used in nested update/delete URLs. Owner membership is
 * created with the project; service rules prevent generic role changes/removal from transferring
 * ownership. joinedAt is populated by Mongo auditing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_members")
public class ProjectMember {

    @Id
    private String id;

    /**
     * Canonical auth-service user ID.
     * Matches the JWT subject ("sub").
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
