package com.taskflow.service;

import com.taskflow.db.TaskRepository;
import com.taskflow.model.Priority;
import com.taskflow.model.Task;
import com.taskflow.model.TaskFilterCriteria;
import com.taskflow.model.TaskStatistics;
import com.taskflow.model.TaskStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceMockitoTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    // Test creating a task with a mocked repository.
    @Test
    void shouldDelegateCreateTaskToRepository() {

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task result = taskService.createTask(
                "Mockito Task",
                "Unit test with Mockito",
                Priority.HOCH,
                LocalDate.of(2026, 12, 1)
        );

        assertNotNull(result.getId());
        assertEquals("Mockito Task", result.getTitle());
        assertEquals(Priority.HOCH, result.getPriority());
        assertEquals(TaskStatus.OFFEN, result.getStatus());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    // Test filtering tasks with criteria using a mocked repository.
    @Test
    void shouldPassFilterCriteriaToRepository() {

        TaskFilterCriteria criteria = new TaskFilterCriteria();
        criteria.setStatus(TaskStatus.OFFEN);

        when(taskRepository.findAll(criteria)).thenReturn(List.of(new Task()));

        List<Task> tasks = taskService.getAllTasks(criteria);

        assertEquals(1, tasks.size());
        verify(taskRepository, times(1)).findAll(criteria);
    }

    // Test statistics mapping including overdue count.
    @Test
    void shouldMapStatisticsIncludingOverdueCount() {

        TaskStatistics stats = new TaskStatistics();
        stats.setTotalCount(5L);
        stats.setOverdueCount(2L);
        stats.getCountByStatus().put(TaskStatus.OFFEN, 3L);
        stats.getCountByStatus().put(TaskStatus.IN_BEARBEITUNG, 1L);
        stats.getCountByStatus().put(TaskStatus.ABGESCHLOSSEN, 1L);

        when(taskRepository.getStatistics()).thenReturn(stats);

        Map<String, Long> result = taskService.getTaskStats();

        assertEquals(5L, result.get("total"));
        assertEquals(3L, result.get("OFFEN"));
        assertEquals(1L, result.get("IN_BEARBEITUNG"));
        assertEquals(1L, result.get("ABGESCHLOSSEN"));
        assertEquals(2L, result.get("overdue"));
    }

    // Test backup export and restore delegation.
    @Test
    void shouldDelegateBackupAndRestoreToRepository() {

        Path dir = Paths.get("backups");
        Path file = dir.resolve("backup.json");

        when(taskRepository.exportBackupToJson(dir)).thenReturn(file);

        Path exported = taskService.exportBackup(dir);
        assertEquals(file, exported);
        verify(taskRepository).exportBackupToJson(dir);

        taskService.restoreBackup(file);
        verify(taskRepository).restoreFromJson(file);
    }

    // Test when repository update returns false.
    @Test
    void shouldReturnEmptyWhenRepositoryUpdateFails() {

        Task existing = new Task();
        existing.setId("task-1");
        when(taskRepository.findById("task-1")).thenReturn(Optional.of(existing));
        when(taskRepository.update(any(Task.class))).thenReturn(false);

        Optional<Task> updated = taskService.updateTask(
                "task-1", "Title", "Desc", Priority.MITTEL, LocalDate.now(), TaskStatus.IN_BEARBEITUNG
        );

        assertTrue(updated.isEmpty());
    }
}
