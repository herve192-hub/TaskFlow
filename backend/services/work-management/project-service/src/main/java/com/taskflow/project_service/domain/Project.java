package com.taskflow.project_service.domain;

import com.taskflow.project_service.domain.enums.ProjectStatus;
import com.taskflow.project_service.domain.enums.ProjectVisibility;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

import java.util.ArrayList;
import java.util.List;

/**
 * Project metadata document keyed independently of auth accounts and user profiles.
 * ownerId is the creator's canonical account ID; membership records determine access. Visibility
 * and lifecycle values are metadata and do not independently bypass current membership checks.
 * Task documents live in another service, so deleting this document is not a task-store cascade.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "projects")
public class Project {

    @Id
    private String id;
    /**
     * Project name.
     */
    private String name;
    /**
     * Project description.
     */
    private String description;
    /**
     * Canonical auth-service account ID of the owner, matching the JWT subject.
     */
    private String ownerId;
    /**
     * Current project lifecycle status.
     */
    @Builder.Default
    private ProjectStatus status = ProjectStatus.PLANNING;
    /**
     * Project visibility.
     */
    @Builder.Default
    private ProjectVisibility visibility = ProjectVisibility.PRIVATE;
    /**
     * Optional project start date.
     */
    private Instant startDate;
    /**
     * Optional project target completion date.
     */
    private Instant targetEndDate;
    /**
     * Timestamp when the project was created.
     */
    @CreatedDate
    private Instant createdAt;
    /**
     * Timestamp when the project was last modified.
     */
    @LastModifiedDate
    private Instant updatedAt;
}
