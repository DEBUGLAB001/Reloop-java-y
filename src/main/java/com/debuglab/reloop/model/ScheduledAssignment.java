package com.debuglab.reloop.model;

import java.time.LocalDateTime;

public class ScheduledAssignment {
    private String jobId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String operation;

    public ScheduledAssignment() {
    }

    public ScheduledAssignment(String jobId, LocalDateTime startTime, LocalDateTime endTime, String operation) {
        this.jobId = jobId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.operation = operation;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
