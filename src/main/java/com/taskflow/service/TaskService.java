
package com.taskflow.service;

import com.taskflow.db.TaskRepository;
import com.taskflow.model.Priority;
import com.taskflow.model.Task;
import com.taskflow.model.TaskFilterCriteria;
import com.taskflow.model.TaskStatistics;
import com.taskflow.model.TaskStatus;
import com.taskflow.model.TaskFilterCriteria;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll(new TaskFilterCriteria());
    }

    public List<Task> getAllTasks(TaskFilterCriteria criteria) {
        return taskRepository.findAll(criteria);
    }

    public Task createTask(String title, String description,
                           Priority priority, LocalDate dueDate) {

        Task task = new Task(
                UUID.randomUUID().toString(),
                title,
                description,
                priority,
                dueDate != null ? dueDate.atStartOfDay() : null,
                TaskStatus.OFFEN,
                LocalDateTime.now()
        );

        return taskRepository.save(task);
    }

    public Optional<Task> getTaskById(String id) {
        return taskRepository.findById(id);
    }

    public Optional<Task> updateTask(
            String id,
            String title,
            String description,
            Priority priority,
            LocalDate dueDate,
            TaskStatus status) {

        Optional<Task> existingTask = taskRepository.findById(id);

        if (existingTask.isEmpty()) {
            return Optional.empty();
        }

        Task task = existingTask.get();

        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setDueDate(dueDate != null ? dueDate.atStartOfDay() : null);
        task.setStatus(status);

        boolean updated = taskRepository.update(task);

        return updated ? taskRepository.findById(id) : Optional.empty();
    }

    public boolean deleteTask(String id) {
        return taskRepository.deleteById(id);
    }

    public Map<String, Long> getTaskStats() {

        TaskStatistics stats = taskRepository.getStatistics();

        return Map.of(
                "total", stats.getTotalCount(),
                "OFFEN", stats.getCountByStatus().getOrDefault(TaskStatus.OFFEN, 0L),
                "IN_BEARBEITUNG", stats.getCountByStatus()
                        .getOrDefault(TaskStatus.IN_BEARBEITUNG, 0L),
                "ABGESCHLOSSEN", stats.getCountByStatus()
                        .getOrDefault(TaskStatus.ABGESCHLOSSEN, 0L)
        );
    }
}
