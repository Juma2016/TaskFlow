
package com.taskflow.service;

import com.taskflow.enums.Priority;
import com.taskflow.enums.TaskStatus;
import com.taskflow.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {

    private TaskService taskService;

    // Create a fresh TaskService before each test.
    @BeforeEach
    void setUp() {
        taskService = new TaskService();
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
                "First Task", "First", Priority.MITTEL, null
        );

        Task secondTask = taskService.createTask(
                "Second Task", "Second", Priority.NIEDRIG, null
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
                "Task One", "First task", Priority.HOCH, null
        );

        taskService.createTask(
                "Task Two", "Second task", Priority.NIEDRIG, null
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

    @Test
    void shouldReturnEmptyWhenTaskIdDoesNotExist() {

        Optional<Task> result =
                taskService.getTaskById(UUID.randomUUID().toString());

        assertTrue(result.isEmpty());
    }

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

    @Test
    void shouldReturnFalseWhenDeletingNonExistingTask() {

        boolean result = taskService.deleteTask(
                UUID.randomUUID().toString()
        );

        assertFalse(result);
    }

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
                task2.getDueDate(),
                TaskStatus.IN_BEARBEITUNG
        );

        taskService.updateTask(
                task3.getId(),
                task3.getTitle(),
                task3.getDescription(),
                task3.getPriority(),
                task3.getDueDate(),
                TaskStatus.ABGESCHLOSSEN
        );

        var stats = taskService.getTaskStats();

        assertEquals(3L, stats.get("total"));
        assertEquals(1L, stats.get("OFFEN"));
        assertEquals(1L, stats.get("IN_BEARBEITUNG"));
        assertEquals(1L, stats.get("ABGESCHLOSSEN"));
    }

    @Test
    void shouldReturnZeroStatsWhenNoTasksExist() {

        var stats = taskService.getTaskStats();

        assertEquals(0L, stats.get("total"));
        assertEquals(0L, stats.get("OFFEN"));
        assertEquals(0L, stats.get("IN_BEARBEITUNG"));
        assertEquals(0L, stats.get("ABGESCHLOSSEN"));
    }
}
