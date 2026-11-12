
package com.taskflow.service;

import com.taskflow.enums.Priority;
import com.taskflow.enums.TaskStatus;
import com.taskflow.model.Task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Handles the business logic for managing tasks.
 * Currently, tasks are stored temporarily in memory.
 */
public class TaskService {

    private final List<Task> tasks = new ArrayList<>();

    /**
     * Returns all tasks currently stored in the system.
     */
    public List<Task> getAllTasks() {
        return tasks;
    }

    /**
     * Creates a new task with a UUID,
     * the default status OFFEN, and the current creation time.
     */
    public Task createTask(String title, String description,
                           Priority priority, LocalDate dueDate) {

        String id = UUID.randomUUID().toString();

        Task task = new Task(
                id,
                title,
                description,
                priority,
                dueDate,
                TaskStatus.OFFEN,
                LocalDateTime.now()
        );

        tasks.add(task);

        return task;
    }

    /**
     * Finds a task by its ID.
     *
     * @param id the ID of the task
     * @return an Optional containing the task if found
     */
    public Optional<Task> getTaskById(String id) {
        return tasks.stream()
                .filter(task -> task.getId().equals(id))
                .findFirst();
    }

    /**
     * Updates an existing task with the provided information.
     *
     * @param id the ID of the task to update
     * @param title the new task title
     * @param description the new task description
     * @param priority the new task priority
     * @param dueDate the new due date
     * @param status the new task status
     * @return an Optional containing the updated task if found
     */
    public Optional<Task> updateTask(
            String id,
            String title,
            String description,
            Priority priority,
            LocalDate dueDate,
            TaskStatus status) {

        Optional<Task> existingTask = getTaskById(id);

        if (existingTask.isEmpty()) {
            return Optional.empty();
        }

        Task task = existingTask.get();

        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setDueDate(dueDate);
        task.setStatus(status);

        return Optional.of(task);
    }

    /**
     * Deletes a task by its ID.
     *
     * @param id the ID of the task to delete
     * @return true if the task was deleted, false if it was not found
     */
    public boolean deleteTask(String id) {
        Optional<Task> task = getTaskById(id);

        if (task.isEmpty()) {
            return false;
        }

        tasks.remove(task.get());
        return true;
    }

    /**
     * Returns statistics about the tasks.
     *
     * @return a map containing the total number of tasks
     * and the number of tasks for each status
     */
    public Map<String, Long> getTaskStats() {

        long total = tasks.size();

        long open = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.OFFEN)
                .count();

        long inProgress = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.IN_BEARBEITUNG)
                .count();

        long completed = tasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.ABGESCHLOSSEN)
                .count();

        return Map.of(
                "total", total,
                "OFFEN", open,
                "IN_BEARBEITUNG", inProgress,
                "ABGESCHLOSSEN", completed
        );
    }
}
