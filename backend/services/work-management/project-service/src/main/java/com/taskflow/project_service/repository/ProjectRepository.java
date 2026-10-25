package com.taskflow.project_service.repository;

import com.taskflow.project_service.domain.Project;
import com.taskflow.project_service.domain.enums.ProjectStatus;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Mongo access for project metadata and owner-scoped case-insensitive name checks.
 * Active list/search flows pass the caller's permitted project IDs before pagination so another
 * user's projects do not leak through a global query. Alternate repository methods do not
 * implicitly carry that authorization filter and require reviewed callers.
 */
public interface ProjectRepository
        extends MongoRepository<Project, String> {

    boolean existsByNameIgnoreCaseAndOwnerId(
            String name,
            String ownerId
    );

    Page<Project> findByOwnerId(
            String ownerId,
            Pageable pageable
    );

    Page<Project> findByStatus(
            ProjectStatus status,
            Pageable pageable
    );

    Page<Project> findByIdIn(
        List<String> ids,
        Pageable pageable
    );
    
    Page<Project> findByOwnerIdAndStatus(
            String ownerId,
            ProjectStatus status,
            Pageable pageable
    );

    Page<Project> findByIdInAndNameContainingIgnoreCase(
        List<String> projectIds,
        String name,
        Pageable pageable
);
}
