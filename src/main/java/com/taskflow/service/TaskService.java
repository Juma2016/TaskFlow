package com.taskflow.service;

import com.taskflow.enums.Priority;
import com.taskflow.enums.TaskStatus;
import com.taskflow.model.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskService {

    private final List<Task> tasks = new ArrayList<>();

    private long nextId = 1;

    public List<Task> getAllTasks() {
        return tasks;
    }

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