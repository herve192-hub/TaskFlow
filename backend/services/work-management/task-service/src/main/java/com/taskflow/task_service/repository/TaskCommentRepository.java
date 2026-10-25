package com.taskflow.task_service.repository;

import com.taskflow.task_service.domain.TaskComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Mongo comment storage and newest-first per-task pagination.
 * TaskServiceImpl first resolves an active task and checks caller view access before using these
 * methods. findByIdAndTaskId is available for scoped future edit/delete work; its existence does
 * not mean those HTTP operations are implemented.
 */
@Repository
public interface TaskCommentRepository
        extends MongoRepository<TaskComment, String> {

    Page<TaskComment> findByTaskIdOrderByCreatedAtDesc(
            String taskId,
            Pageable pageable
    );

    Optional<TaskComment> findByIdAndTaskId(
            String id,
            String taskId
    );

    long countByTaskId(
            String taskId
    );

    void deleteByTaskId(
            String taskId
    );
}
