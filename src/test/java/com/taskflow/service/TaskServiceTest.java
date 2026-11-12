
package com.taskflow.service;

import com.taskflow.db.DatabaseManager;
import com.taskflow.db.SqliteTaskRepository;
import com.taskflow.model.Priority;
import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {

    private TaskService taskService;

    @TempDir
    Path tempDir;

    // Create a fresh SQLite database before each test.
    @BeforeEach
    void setUp() {

        DatabaseManager databaseManager = new DatabaseManager(
                "jdbc:sqlite:" + tempDir.resolve("test-taskflow.db").toAbsolutePath()
        );

        SqliteTaskRepository repository =
                new SqliteTaskRepository(databaseManager);

        taskService = new TaskService(repository);
    }

    // Test whether a task is created correctly.
    @Test
    void shouldCreateTaskSuccessfully() {

        Task task = taskService.createTask(
                "Test Task",
                "Testing task creation",
                Priority.HOCH,
                null
        );

        assertNotNull(task);
        assertNotNull(task.getId());
        assertDoesNotThrow(() -> UUID.fromString(task.getId()));
        assertEquals("Test Task", task.getTitle());
        assertEquals("Testing task creation", task.getDescription());
        assertEquals(Priority.HOCH, task.getPriority());
        assertEquals(TaskStatus.OFFEN, task.getStatus());
        assertNotNull(task.getCreatedAt());
    }

    // Test whether a new task receives a unique UUID.
    @Test
    void shouldGenerateUniqueIds() {

        Task firstTask = taskService.createTask(
                "First Task",
                "First",
                Priority.MITTEL,
                null
        );

        Task secondTask = taskService.createTask(
                "Second Task",
                "Second",
                Priority.NIEDRIG,
                null
        );

        assertNotNull(firstTask.getId());
        assertNotNull(secondTask.getId());
        assertNotEquals(firstTask.getId(), secondTask.getId());

        assertDoesNotThrow(() -> UUID.fromString(firstTask.getId()));
        assertDoesNotThrow(() -> UUID.fromString(secondTask.getId()));
    }

    // Test whether all created tasks are returned.
    @Test
    void shouldReturnAllTasks() {

        taskService.createTask(
                "Task One",
                "First task",
                Priority.HOCH,
                null
        );

        taskService.createTask(
                "Task Two",
                "Second task",
                Priority.NIEDRIG,
                null
        );

        List<Task> tasks = taskService.getAllTasks();

        assertEquals(2, tasks.size());
    }

    // Test whether an empty task list is returned initially.
    @Test
    void shouldReturnEmptyListInitially() {

        List<Task> tasks = taskService.getAllTasks();

        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }

    // Test whether a task can be found by its ID.
    @Test
    void shouldFindTaskById() {

        Task createdTask = taskService.createTask(
                "Test Task",
                "Testing getTaskById",
                Priority.HOCH,
                null
        );

        Optional<Task> result =
                taskService.getTaskById(createdTask.getId());

        assertTrue(result.isPresent());
        assertEquals("Test Task", result.get().getTitle());
    }

    // Test whether an unknown task ID returns an empty Optional.
    @Test
    void shouldReturnEmptyWhenTaskIdDoesNotExist() {

        Optional<Task> result =
                taskService.getTaskById(UUID.randomUUID().toString());

        assertTrue(result.isEmpty());
    }

    // Test whether an existing task can be updated.
    @Test
    void shouldUpdateTaskSuccessfully() {

        Task task = taskService.createTask(
                "Old Title",
                "Old Description",
                Priority.NIEDRIG,
                null
        );

        Optional<Task> result = taskService.updateTask(
                task.getId(),
                "New Title",
                "New Description",
                Priority.HOCH,
                null,
                TaskStatus.IN_BEARBEITUNG
        );

        assertTrue(result.isPresent());
        assertEquals("New Title", result.get().getTitle());
        assertEquals("New Description", result.get().getDescription());
        assertEquals(Priority.HOCH, result.get().getPriority());
        assertEquals(TaskStatus.IN_BEARBEITUNG, result.get().getStatus());
    }

    // Test whether updating a non-existing task returns an empty Optional.
    @Test
    void shouldReturnEmptyWhenUpdatingNonExistingTask() {

        Optional<Task> result = taskService.updateTask(
                UUID.randomUUID().toString(),
                "New Title",
                "New Description",
                Priority.HOCH,
                null,
                TaskStatus.OFFEN
        );

        assertTrue(result.isEmpty());
    }

    // Test whether an existing task can be deleted.
    @Test
    void shouldDeleteTaskSuccessfully() {

        Task task = taskService.createTask(
                "Task to Delete",
                "This task will be deleted",
                Priority.HOCH,
                null
        );

        boolean result = taskService.deleteTask(task.getId());

        assertTrue(result);
        assertTrue(taskService.getTaskById(task.getId()).isEmpty());
    }

    // Test whether deleting a non-existing task returns false.
    @Test
    void shouldReturnFalseWhenDeletingNonExistingTask() {

        boolean result = taskService.deleteTask(
                UUID.randomUUID().toString()
        );

        assertFalse(result);
    }

    // Test whether task statistics are calculated correctly.
    @Test
    void shouldReturnCorrectTaskStats() {

        taskService.createTask(
                "Open Task",
                "Test open status",
                Priority.NIEDRIG,
                null
        );

        Task task2 = taskService.createTask(
                "In Progress Task",
                "Test in progress status",
                Priority.MITTEL,
                null
        );

        Task task3 = taskService.createTask(
                "Completed Task",
                "Test completed status",
                Priority.HOCH,
                null
        );

        taskService.updateTask(
                task2.getId(),
                task2.getTitle(),
                task2.getDescription(),
                task2.getPriority(),
                null,
                TaskStatus.IN_BEARBEITUNG
        );

        taskService.updateTask(
                task3.getId(),
                task3.getTitle(),
                task3.getDescription(),
                task3.getPriority(),
                null,
                TaskStatus.ABGESCHLOSSEN
        );

        var stats = taskService.getTaskStats();

        assertEquals(3L, stats.get("total"));
        assertEquals(1L, stats.get("OFFEN"));
        assertEquals(1L, stats.get("IN_BEARBEITUNG"));
        assertEquals(1L, stats.get("ABGESCHLOSSEN"));
    }

    // Test whether all statistics are zero when no tasks exist.
    @Test
    void shouldReturnZeroStatsWhenNoTasksExist() {

        var stats = taskService.getTaskStats();

        assertEquals(0L, stats.get("total"));
        assertEquals(0L, stats.get("OFFEN"));
        assertEquals(0L, stats.get("IN_BEARBEITUNG"));
        assertEquals(0L, stats.get("ABGESCHLOSSEN"));
    }
}
