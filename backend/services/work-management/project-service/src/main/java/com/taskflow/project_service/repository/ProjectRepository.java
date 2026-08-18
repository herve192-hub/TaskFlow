package com.taskflow.project_service.repository;

import com.taskflow.project_service.domain.Project;
import com.taskflow.project_service.domain.enums.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface ProjectRepository extends MongoRepository<Project, String> {

    Optional<Project> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndOwnerId(String name, String ownerId);

    @Query("{ $or: [ { ownerId: ?0 }, { memberIds: ?0 } ] }")
    Page<Project> findProjectsForUser(String userId, Pageable pageable);

    @Query("{ $and: [ { $or: [ { ownerId: ?0 }, { memberIds: ?0 } ] }, { $or: [ { name: { $regex: ?1, $options: 'i' } }, { description: { $regex: ?1, $options: 'i' } } ] } ] }")
    Page<Project> searchProjectsForUser(String userId, String search, Pageable pageable);

    Page<Project> findByOwnerId(String ownerId, Pageable pageable);

    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);

    Page<Project> findByOwnerIdAndStatus(String ownerId, ProjectStatus status, Pageable pageable);
}