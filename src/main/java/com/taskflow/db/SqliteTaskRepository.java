package com.taskflow.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskflow.model.Priority;
import com.taskflow.model.Task;
import com.taskflow.model.TaskFilterCriteria;
import com.taskflow.model.TaskStatistics;
import com.taskflow.model.TaskStatus;
import com.taskflow.model.TaskStatusHistory;

public class SqliteTaskRepository implements TaskRepository {

    private final DatabaseManager dbManager;
    private final ObjectMapper objectMapper;

    public SqliteTaskRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public Task save(Task task) {
        if (task.getId() == null || task.getId().isBlank()) {
            task.setId(UUID.randomUUID().toString());
        }
        if (task.getCreatedAt() == null) {
            task.setCreatedAt(LocalDateTime.now());
        }
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.OFFEN);
        }
        if (task.getPriority() == null) {
            task.setPriority(Priority.MITTEL);
        }

        String insertTaskSql = """
                INSERT INTO tasks (id, title, description, priority, status, due_date, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        String insertHistorySql = """
                INSERT INTO task_status_history (task_id, old_status, new_status, changed_at)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement taskStmt = conn.prepareStatement(insertTaskSql);
                 PreparedStatement historyStmt = conn.prepareStatement(insertHistorySql)) {

                taskStmt.setString(1, task.getId());
                taskStmt.setString(2, task.getTitle());
                taskStmt.setString(3, task.getDescription());
                taskStmt.setString(4, task.getPriority().name());
                taskStmt.setString(5, task.getStatus().name());
                taskStmt.setString(6, task.getDueDate() != null ? task.getDueDate().toString() : null);
                taskStmt.setString(7, task.getCreatedAt().toString());
                taskStmt.executeUpdate();

                // Record the creation history of the initial state
                historyStmt.setString(1, task.getId());
                historyStmt.setNull(2, Types.VARCHAR);
                historyStmt.setString(3, task.getStatus().name());
                historyStmt.setString(4, task.getCreatedAt().toString());
                historyStmt.executeUpdate();

                conn.commit();
                return task;
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Failed to save task; transaction has been rolled back.: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database connection error: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Task> findById(String id) {
        String sql = "SELECT id, title, description, priority, status, due_date, created_at FROM tasks WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Task task = mapRowToTask(rs);
                task.setHistory(findHistoryByTaskId(conn, id));
                return Optional.of(task);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find task by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Task> findAll(TaskFilterCriteria criteria) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, title, description, priority, status, due_date, created_at FROM tasks WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (criteria != null) {
            if (criteria.getStatus() != null) {
                sql.append(" AND status = ?");
                params.add(criteria.getStatus().name());
            }
            if (criteria.getPriority() != null) {
                sql.append(" AND priority = ?");
                params.add(criteria.getPriority().name());
            }
            if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
                sql.append(" AND (LOWER(title) LIKE ? OR LOWER(description) LIKE ?)");
                String pattern = "%" + criteria.getKeyword().toLowerCase().trim() + "%";
                params.add(pattern);
                params.add(pattern);
            }
            if (criteria.getDueFilter() != null) {
                LocalDateTime now = LocalDateTime.now();
                switch (criteria.getDueFilter()) {
                    case OVERDUE -> {
                        sql.append(" AND due_date IS NOT NULL AND due_date < ? AND status != 'ABGESCHLOSSEN'");
                        params.add(now.toString());
                    }
                    case TODAY -> {
                        LocalDate today = LocalDate.now();
                        sql.append(" AND due_date >= ? AND due_date <= ?");
                        params.add(today.atStartOfDay().toString());
                        params.add(today.atTime(LocalTime.MAX).toString());
                    }
                    case THIS_WEEK -> {
                        LocalDate today = LocalDate.now();
                        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                        sql.append(" AND due_date >= ? AND due_date <= ?");
                        params.add(startOfWeek.atStartOfDay().toString());
                        params.add(endOfWeek.atTime(LocalTime.MAX).toString());
                    }
                }
            }
        }

        // Default sort: by priority (HIGH -> MEDIUM -> LOW), then by due date in ascending order (items with no due date appear last)
        sql.append("""
                 ORDER BY CASE priority
                    WHEN 'HOCH' THEN 1
                    WHEN 'MITTEL' THEN 2
                    WHEN 'NIEDRIG' THEN 3
                    ELSE 4
                END ASC,
                CASE WHEN due_date IS NULL THEN 1 ELSE 0 END ASC,
                due_date ASC
                """);

        List<Task> tasks = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRowToTask(rs));
                }
            }
            return tasks;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to filter and query task list: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Task task) {
        String selectOldStatusSql = "SELECT status FROM tasks WHERE id = ?";
        String updateTaskSql = """
                UPDATE tasks
                SET title = ?, description = ?, priority = ?, status = ?, due_date = ?
                WHERE id = ?
                """;
        String insertHistorySql = """
                INSERT INTO task_status_history (task_id, old_status, new_status, changed_at)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                TaskStatus oldStatus = null;
                try (PreparedStatement selectStmt = conn.prepareStatement(selectOldStatusSql)) {
                    selectStmt.setString(1, task.getId());
                    try (ResultSet rs = selectStmt.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        oldStatus = TaskStatus.valueOf(rs.getString("status"));
                    }
                }

                try (PreparedStatement updateStmt = conn.prepareStatement(updateTaskSql)) {
                    updateStmt.setString(1, task.getTitle());
                    updateStmt.setString(2, task.getDescription());
                    updateStmt.setString(3, task.getPriority().name());
                    updateStmt.setString(4, task.getStatus().name());
                    updateStmt.setString(5, task.getDueDate() != null ? task.getDueDate().toString() : null);
                    updateStmt.setString(6, task.getId());
                    updateStmt.executeUpdate();
                }

                // If the status changes, it is automatically written to the history table.
                if (oldStatus != task.getStatus()) {
                    try (PreparedStatement historyStmt = conn.prepareStatement(insertHistorySql)) {
                        historyStmt.setString(1, task.getId());
                        historyStmt.setString(2, oldStatus.name());
                        historyStmt.setString(3, task.getStatus().name());
                        historyStmt.setString(4, LocalDateTime.now().toString());
                        historyStmt.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Failed to update task; transaction has been rolled back: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database connection error: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        // The `ON DELETE CASCADE` foreign key will automatically clean up associated records in `task_status_history`.
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete task: " + e.getMessage(), e);
        }
    }

    @Override
    public TaskStatistics getStatistics() {
        TaskStatistics stats = new TaskStatistics();
        for (TaskStatus s : TaskStatus.values()) {
            stats.getCountByStatus().put(s, 0L);
        }

        String statusCountSql = "SELECT status, COUNT(*) AS cnt FROM tasks GROUP BY status";
        String overdueCountSql = "SELECT COUNT(*) AS cnt FROM tasks WHERE due_date IS NOT NULL AND due_date < ? AND status != 'ABGESCHLOSSEN'";

        try (Connection conn = dbManager.getConnection()) {
            long total = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(statusCountSql)) {
                while (rs.next()) {
                    TaskStatus status = TaskStatus.valueOf(rs.getString("status"));
                    long cnt = rs.getLong("cnt");
                    stats.getCountByStatus().put(status, cnt);
                    total += cnt;
                }
            }
            stats.setTotalCount(total);

            try (PreparedStatement stmt = conn.prepareStatement(overdueCountSql)) {
                stmt.setString(1, LocalDateTime.now().toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats.setOverdueCount(rs.getLong("cnt"));
                    }
                }
            }
            return stats;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve task statistics: " + e.getMessage(), e);
        }
    }

    @Override
    public Path exportBackupToJson(Path directory) {
        try {
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // Query all tasks and their complete status history
            List<Task> allTasks = findAll(null);
            try (Connection conn = dbManager.getConnection()) {
                for (Task task : allTasks) {
                    task.setHistory(findHistoryByTaskId(conn, task.getId()));
                }
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path targetFile = directory.resolve("taskflow_backup_" + timestamp + ".json");
            Path tempFile = directory.resolve("taskflow_backup_" + timestamp + ".tmp");

            // Write to a temporary file first, then perform an atomic rename, ensuring zero corruption in the event of a crash.
            objectMapper.writeValue(tempFile.toFile(), allTasks);
            Files.move(tempFile, targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

            return targetFile;
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Failed to export JSON backup: " + e.getMessage(), e);
        }
    }

    @Override
    public void restoreFromJson(Path backupFile) {
        try {
            List<Task> tasks = objectMapper.readValue(backupFile.toFile(), new TypeReference<>() {});

            String clearTasksSql = "DELETE FROM tasks";
            String insertTaskSql = """
                    INSERT INTO tasks (id, title, description, priority, status, due_date, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;
            String insertHistorySql = """
                    INSERT INTO task_status_history (task_id, old_status, new_status, changed_at)
                    VALUES (?, ?, ?, ?)
                    """;

            try (Connection conn = dbManager.getConnection()) {
                conn.setAutoCommit(false);
                try (Statement clearStmt = conn.createStatement();
                     PreparedStatement taskStmt = conn.prepareStatement(insertTaskSql);
                     PreparedStatement historyStmt = conn.prepareStatement(insertHistorySql)) {

                    clearStmt.execute(clearTasksSql);

                    for (Task task : tasks) {
                        taskStmt.setString(1, task.getId());
                        taskStmt.setString(2, task.getTitle());
                        taskStmt.setString(3, task.getDescription());
                        taskStmt.setString(4, task.getPriority().name());
                        taskStmt.setString(5, task.getStatus().name());
                        taskStmt.setString(6, task.getDueDate() != null ? task.getDueDate().toString() : null);
                        taskStmt.setString(7, task.getCreatedAt().toString());
                        taskStmt.addBatch();

                        if (task.getHistory() != null) {
                            for (TaskStatusHistory h : task.getHistory()) {
                                historyStmt.setString(1, task.getId());
                                if (h.getOldStatus() != null) {
                                    historyStmt.setString(2, h.getOldStatus().name());
                                } else {
                                    historyStmt.setNull(2, Types.VARCHAR);
                                }
                                historyStmt.setString(3, h.getNewStatus().name());
                                historyStmt.setString(4, h.getChangedAt().toString());
                                historyStmt.addBatch();
                            }
                        }
                    }

                    taskStmt.executeBatch();
                    historyStmt.executeBatch();
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw new RuntimeException("Failed to restore backup; transaction has been rolled back: " + e.getMessage(), e);
                }
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Failed to read or restore JSON backup file: " + e.getMessage(), e);
        }
    }

    private Task mapRowToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getString("id"));
        task.setTitle(rs.getString("title"));
        task.setDescription(rs.getString("description"));
        task.setPriority(Priority.valueOf(rs.getString("priority")));
        task.setStatus(TaskStatus.valueOf(rs.getString("status")));

        String dueDateStr = rs.getString("due_date");
        if (dueDateStr != null && !dueDateStr.isBlank()) {
            task.setDueDate(LocalDateTime.parse(dueDateStr));
        }
        task.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        return task;
    }

    private List<TaskStatusHistory> findHistoryByTaskId(Connection conn, String taskId) throws SQLException {
        String sql = "SELECT id, task_id, old_status, new_status, changed_at FROM task_status_history WHERE task_id = ? ORDER BY id ASC";
        List<TaskStatusHistory> historyList = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, taskId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String oldStatusStr = rs.getString("old_status");
                    TaskStatus oldStatus = (oldStatusStr != null) ? TaskStatus.valueOf(oldStatusStr) : null;
                    TaskStatusHistory h = new TaskStatusHistory(
                            rs.getLong("id"),
                            rs.getString("task_id"),
                            oldStatus,
                            TaskStatus.valueOf(rs.getString("new_status")),
                            LocalDateTime.parse(rs.getString("changed_at"))
                    );
                    historyList.add(h);
                }
            }
        }
        return historyList;
    }
}