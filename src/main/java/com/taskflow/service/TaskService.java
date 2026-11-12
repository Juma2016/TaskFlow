package com.taskflow.service;

import com.taskflow.enums.Priority;
import com.taskflow.enums.TaskStatus;
import com.taskflow.model.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the business logic for managing tasks.
 * Currently, tasks are stored temporarily in memory.
 */

public class TaskService {

    private final List<Task> tasks = new ArrayList<>();

    private long nextId = 1;

    /**
     * Returns all tasks currently stored in the system.
     */
    public List<Task> getAllTasks() {
        return tasks;
    }

    /**
     * Creates a new task with an automatically generated ID,
     * the default status OFFEN, and the current creation time.
     */
    public Task createTask(String title, String description,
                           Priority priority) {

        Task task = new Task(
                nextId++,
                title,
                description,
                priority,
                null,
                TaskStatus.OFFEN,
                LocalDateTime.now()
        );

        tasks.add(task);

        return task;
    }
}