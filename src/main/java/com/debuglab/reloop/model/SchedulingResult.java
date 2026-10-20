package com.debuglab.reloop.model;

import java.time.LocalDateTime;

public class SchedulingResult {
    private boolean successful;
    private String technicianId;
    private String jobId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String message;

    public SchedulingResult() {
    }

    public SchedulingResult(boolean successful, String technicianId, String jobId,
                            LocalDateTime startTime, LocalDateTime endTime, String message) {
        this.successful = successful;
        this.technicianId = technicianId;
        this.jobId = jobId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.message = message;
    }

    public static SchedulingResult success(String technicianId, String jobId,
                                           LocalDateTime startTime, LocalDateTime endTime, String message) {
        return new SchedulingResult(true, technicianId, jobId, startTime, endTime, message);
    }

    public static SchedulingResult failure(String jobId, String message) {
        return new SchedulingResult(false, null, jobId, null, null, message);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(String technicianId) {
        this.technicianId = technicianId;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
