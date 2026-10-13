package com.taskflow.task_service.repository;

import com.taskflow.task_service.domain.Task;
import com.taskflow.task_service.domain.enums.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class TaskRepositoryIT {
    @Autowired
    private TaskRepository repository;

    private final String projectId = UUID.randomUUID().toString();
    private final List<Task> savedTasks = new ArrayList<>();

    @AfterEach
    void removeTestData() {
        repository.deleteAll(savedTasks);
    }

    @Test
    void projectQueriesExcludeArchivedTasksAndOtherProjects() {
        Task visible = saveTask(projectId, "Visible task", TaskStatus.TODO, false);
        saveTask(projectId, "Archived task", TaskStatus.TODO, true);
        saveTask(UUID.randomUUID().toString(), "Other project", TaskStatus.TODO, false);

        assertThat(repository.findByProjectIdAndArchivedFalse(projectId, PageRequest.of(0, 20)).getContent())
                .extracting(Task::getId).containsExactly(visible.getId());
        assertThat(repository.countByProjectIdAndArchivedFalse(projectId)).isEqualTo(1);
        assertThat(repository.findById(visible.getId())).get()
                .extracting(Task::getTitle).isEqualTo("Visible task");
    }

    @Test
    void duplicateTitleChecksAreCaseInsensitiveAndIgnoreArchivedTasks() {
        saveTask(projectId, "Review CI", TaskStatus.TODO, false);
        saveTask(projectId, "Old task", TaskStatus.TODO, true);

        assertThat(repository.existsByProjectIdAndTitleIgnoreCaseAndArchivedFalse(projectId, "review ci"))
                .isTrue();
        assertThat(repository.existsByProjectIdAndTitleIgnoreCaseAndArchivedFalse(projectId, "old task"))
                .isFalse();
    }

    @Test
    void statusQueriesReflectPersistedLifecycleChanges() {
        Task task = saveTask(projectId, "Finish CI", TaskStatus.TODO, false);
        task.setStatus(TaskStatus.COMPLETED);
        repository.save(task);

        assertThat(repository.countByProjectIdAndStatusAndArchivedFalse(projectId, TaskStatus.TODO))
                .isZero();
        assertThat(repository.findByProjectIdAndStatusAndArchivedFalse(
                projectId, TaskStatus.COMPLETED, PageRequest.of(0, 20)).getContent())
                .extracting(Task::getId).containsExactly(task.getId());
    }

    private Task saveTask(String project, String title, TaskStatus status, boolean archived) {
        Task task = repository.save(Task.builder().projectId(project).title(title)
                .status(status).archived(archived).build());
        savedTasks.add(task);
        return task;
    }
}
