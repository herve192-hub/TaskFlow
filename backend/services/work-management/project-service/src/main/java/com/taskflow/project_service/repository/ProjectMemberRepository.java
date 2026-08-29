package com.taskflow.project_service.repository;

import com.taskflow.project_service.domain.ProjectMember;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

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