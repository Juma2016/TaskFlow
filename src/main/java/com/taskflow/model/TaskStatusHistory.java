package com.taskflow.model;

import java.time.LocalDateTime;

public class TaskStatusHistory {
    private Long id;
    private String taskId;
    private TaskStatus oldStatus;
    private TaskStatus newStatus;
    private LocalDateTime changedAt;

    public TaskStatusHistory() {
    }

    public TaskStatusHistory(Long id, String taskId, TaskStatus oldStatus, TaskStatus newStatus, LocalDateTime changedAt) {
        this.id = id;
        this.taskId = taskId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedAt = changedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public TaskStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(TaskStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public TaskStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(TaskStatus newStatus) {
        this.newStatus = newStatus;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
