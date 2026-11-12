package com.taskflow.db;

import com.taskflow.model.Task;
import com.taskflow.model.TaskFilterCriteria;
import com.taskflow.model.TaskStatistics;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    Task save(Task task);
    Optional<Task> findById(String id);
    List<Task> findAll(TaskFilterCriteria criteria);
    boolean update(Task task);
    boolean deleteById(String id);
    TaskStatistics getStatistics();
    Path exportBackupToJson(Path directory);
    void restoreFromJson(Path backupFile);
}
