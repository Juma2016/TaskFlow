
package com.taskflow.service;

import com.taskflow.enums.Priority;
import com.taskflow.enums.TaskStatus;
import com.taskflow.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

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
        assertEquals(1L, task.getId());
        assertEquals("Test Task", task.getTitle());
        assertEquals("Testing task creation", task.getDescription());
        assertEquals(Priority.HOCH, task.getPriority());
        assertEquals(TaskStatus.OFFEN, task.getStatus());
        assertNotNull(task.getCreatedAt());
    }

    // Test whether a new task receives a unique ID.
    @Test
    void shouldGenerateUniqueIds() {

        Task firstTask = taskService.createTask(
                "First Task", "First", Priority.MITTEL, null
        );

        Task secondTask = taskService.createTask(
                "Second Task", "Second", Priority.NIEDRIG, null
        );

        assertEquals(1L, firstTask.getId());
        assertEquals(2L, secondTask.getId());
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

        Optional<Task> result = taskService.getTaskById(createdTask.getId());

        assertTrue(result.isPresent());
        assertEquals("Test Task", result.get().getTitle());
    }

    @Test
    void shouldReturnEmptyWhenTaskIdDoesNotExist() {

        Optional<Task> result = taskService.getTaskById(999L);

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
                999L,
                "New Title",
                "New Description",
                Priority.HOCH,
                null,
                TaskStatus.OFFEN
        );

        assertTrue(result.isEmpty());
    }
}
