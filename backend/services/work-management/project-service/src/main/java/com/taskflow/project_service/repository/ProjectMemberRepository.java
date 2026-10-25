package com.taskflow.project_service.repository;

import com.taskflow.project_service.domain.ProjectMember;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Mongo queries for project-team membership, caller-project scope, counts, and removals.
 * ProjectServiceImpl uses projectId/userId pairs to derive permissions and validates a membership
 * belongs to the route's project before mutation. Repository methods alone do not authorize callers.
 */
public interface ProjectMemberRepository
        extends MongoRepository<ProjectMember, String> {

    List<ProjectMember> findByProjectId(
            String projectId
    );

    Page<ProjectMember> findByProjectId(
            String projectId,
            Pageable pageable
    );

    List<ProjectMember> findByUserId(
            String userId
    );

    Optional<ProjectMember> findByProjectIdAndUserId(
            String projectId,
            String userId
    );

    boolean existsByProjectIdAndUserId(
            String projectId,
            String userId
    );

    long countByProjectId(
            String projectId
    );

    void deleteByProjectId(
            String projectId
    );

    void deleteByProjectIdAndUserId(
            String projectId,
            String userId
    );
}
