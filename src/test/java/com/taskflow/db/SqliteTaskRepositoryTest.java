package com.taskflow.db;

import com.taskflow.model.Priority;
import com.taskflow.model.Task;
import com.taskflow.model.TaskFilterCriteria;
import com.taskflow.model.TaskStatistics;
import com.taskflow.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SqliteTaskRepositoryTest {

    private SqliteTaskRepository repository;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        DatabaseManager dbManager = new DatabaseManager(
                "jdbc:sqlite:" + tempDir.resolve("repo-test.db").toAbsolutePath()
        );
        repository = new SqliteTaskRepository(dbManager);
    }

    // Test saving a task with default values and recording status history.
    @Test
    void shouldSaveTaskWithDefaultsAndHistory() {

        Task task = new Task();
        task.setTitle("Default Task");

        Task saved = repository.save(task);

        assertNotNull(saved.getId());
        assertEquals(Priority.MITTEL, saved.getPriority());
        assertEquals(TaskStatus.OFFEN, saved.getStatus());
        assertNotNull(saved.getCreatedAt());

        Optional<Task> found = repository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(1, found.get().getHistory().size());
        assertNull(found.get().getHistory().get(0).getOldStatus());
        assertEquals(TaskStatus.OFFEN, found.get().getHistory().get(0).getNewStatus());
    }

    // Test filtering by status, priority, keyword, and due date (OVERDUE, TODAY, THIS_WEEK).
    @Test
    void shouldFilterTasksByCriteria() {

        Task overdueTask = new Task(
                null, "Overdue Task", "Urgent bugfix",
                Priority.HOCH, LocalDate.now().minusDays(2).atStartOfDay(),
                TaskStatus.OFFEN, null
        );
        repository.save(overdueTask);

        Task todayTask = new Task(
                null, "Today Task", "Daily meeting",
                Priority.MITTEL, LocalDate.now().atStartOfDay(),
                TaskStatus.IN_BEARBEITUNG, null
        );
        repository.save(todayTask);

        // Filter OVERDUE: should only return overdueTask, not todayTask.
        TaskFilterCriteria overdueCriteria = new TaskFilterCriteria();
        overdueCriteria.setDueFilter(TaskFilterCriteria.DueFilter.OVERDUE);
        List<Task> overdueList = repository.findAll(overdueCriteria);
        assertEquals(1, overdueList.size());
        assertEquals("Overdue Task", overdueList.get(0).getTitle());

        // Filter TODAY: should only return todayTask.
        TaskFilterCriteria todayCriteria = new TaskFilterCriteria();
        todayCriteria.setDueFilter(TaskFilterCriteria.DueFilter.TODAY);
        List<Task> todayList = repository.findAll(todayCriteria);
        assertEquals(1, todayList.size());
        assertEquals("Today Task", todayList.get(0).getTitle());

        // Filter THIS_WEEK.
        TaskFilterCriteria weekCriteria = new TaskFilterCriteria();
        weekCriteria.setDueFilter(TaskFilterCriteria.DueFilter.THIS_WEEK);
        List<Task> weekList = repository.findAll(weekCriteria);
        assertFalse(weekList.isEmpty());

        // Filter by keyword, priority, and status.
        TaskFilterCriteria combined = new TaskFilterCriteria();
        combined.setStatus(TaskStatus.OFFEN);
        combined.setPriority(Priority.HOCH);
        combined.setKeyword("bugfix");
        List<Task> filtered = repository.findAll(combined);
        assertEquals(1, filtered.size());
        assertEquals("Overdue Task", filtered.get(0).getTitle());

        // Verify statistics overdue count.
        TaskStatistics stats = repository.getStatistics();
        assertEquals(2L, stats.getTotalCount());
        assertEquals(1L, stats.getOverdueCount());
    }

    // Test JSON backup export and restore functionality.
    @Test
    void shouldExportAndRestoreJsonBackup() {

        Task task = new Task(
                null, "Backup Task", "Important data",
                Priority.HOCH, LocalDate.now().plusDays(1).atStartOfDay(),
                TaskStatus.OFFEN, null
        );
        Task saved = repository.save(task);

        saved.setStatus(TaskStatus.IN_BEARBEITUNG);
        repository.update(saved);

        Path backupDir = tempDir.resolve("backups");
        Path backupFile = repository.exportBackupToJson(backupDir);

        assertTrue(Files.exists(backupFile));

        // Delete the task from DB and restore from JSON backup.
        repository.deleteById(saved.getId());
        assertTrue(repository.findById(saved.getId()).isEmpty());

        repository.restoreFromJson(backupFile);

        Optional<Task> restored = repository.findById(saved.getId());
        assertTrue(restored.isPresent());
        assertEquals("Backup Task", restored.get().getTitle());
        assertEquals(TaskStatus.IN_BEARBEITUNG, restored.get().getStatus());
        assertEquals(2, restored.get().getHistory().size());
    }

    // Test filter performance (< 100 ms) as defined in the quality plan.
    @Test
    void shouldFilterTasksWithin100Milliseconds() {

        for (int i = 0; i < 200; i++) {
            Task t = new Task(
                    null, "Task " + i, "Description " + i,
                    i % 2 == 0 ? Priority.HOCH : Priority.NIEDRIG,
                    LocalDate.now().plusDays(i % 10).atStartOfDay(),
                    TaskStatus.OFFEN, null
            );
            repository.save(t);
        }

        TaskFilterCriteria criteria = new TaskFilterCriteria();
        criteria.setStatus(TaskStatus.OFFEN);
        criteria.setPriority(Priority.HOCH);

        long startNanos = System.nanoTime();
        List<Task> result = repository.findAll(criteria);
        long durationMs = (System.nanoTime() - startNanos) / 1_000_000;

        assertEquals(100, result.size());
        assertTrue(durationMs <= 100, "Filter took " + durationMs + " ms (expected <= 100 ms)");
    }
}
