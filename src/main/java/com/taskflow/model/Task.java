package com.taskflow.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Task {

    private String id;
    private String title;
    private String description;
    private Priority priority;
    private LocalDateTime dueDate;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private List<TaskStatusHistory> history = new ArrayList<>();

    public Task() {
    }

    public Task(String id, String title, String description,
                Priority priority, LocalDateTime dueDate,
                TaskStatus status, LocalDateTime createdAt) {

        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.dueDate = dueDate;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<TaskStatusHistory> getHistory() {
        return history;
    }

    public void setHistory(List<TaskStatusHistory> history) {
        this.history = history != null ? history : new ArrayList<>();
    }
}
