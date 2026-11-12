package com.taskflow.model;

public class TaskFilterCriteria {
    public enum DueFilter {
        OVERDUE,    // überfällig
        TODAY,      // heute
        THIS_WEEK   // diese Woche
    }

    private TaskStatus status;
    private Priority priority;
    private DueFilter dueFilter;
    private String keyword;

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public DueFilter getDueFilter() {
        return dueFilter;
    }

    public void setDueFilter(DueFilter dueFilter) {
        this.dueFilter = dueFilter;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}