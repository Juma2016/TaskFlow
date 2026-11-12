package com.taskflow.model;

import java.util.EnumMap;
import java.util.Map;

public class TaskStatistics {
    private Map<TaskStatus, Long> countByStatus = new EnumMap<>(TaskStatus.class);
    private long overdueCount;
    private long totalCount;

    public Map<TaskStatus, Long> getCountByStatus() {
        return countByStatus;
    }

    public void setCountByStatus(Map<TaskStatus, Long> countByStatus) {
        this.countByStatus = countByStatus;
    }

    public long getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(long overdueCount) {
        this.overdueCount = overdueCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
}
