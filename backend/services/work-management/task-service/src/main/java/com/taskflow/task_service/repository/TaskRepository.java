package com.taskflow.task_service.repository;

import com.taskflow.task_service.domain.Task;
import com.taskflow.task_service.domain.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends MongoRepository<Task, String> {

    Page<Task> findByProjectIdAndArchivedFalse(
            String projectId,
            Pageable pageable
    );

    Page<Task> findByAssigneeIdAndArchivedFalse(
            String assigneeId,
            Pageable pageable
    );

    Page<Task> findByCreatedByAndArchivedFalse(
            String createdBy,
            Pageable pageable
    );

    Page<Task> findByProjectIdAndStatusAndArchivedFalse(
            String projectId,
            TaskStatus status,
            Pageable pageable
    );

    List<Task> findByProjectIdAndArchivedFalse(
            String projectId
    );

    List<Task> findByParentTaskIdAndArchivedFalse(
            String parentTaskId
    );

    long countByProjectIdAndArchivedFalse(
            String projectId
    );

    long countByProjectIdAndStatusAndArchivedFalse(
            String projectId,
            TaskStatus status
    );

    boolean existsByProjectIdAndTitleIgnoreCaseAndArchivedFalse(
            String projectId,
            String title
    );

    boolean existsByIdAndProjectIdAndArchivedFalse(
            String id,
            String projectId
    );
}