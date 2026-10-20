package com.debuglab.reloop.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RecoveryJob {
    private String id;
    private String title;
    private String sourceDeviceId;
    private String requiredOperation;
    private List<String> recoverableComponentIds = new ArrayList<>();
    private double priorityScore;
    private UrgencyLevel urgency;
    private double estimatedRecoveryValue;
    private int processingDurationMinutes;
    private int requiredCapacityUnits;
    private TechnicianSkill requiredTechnicianSkill;
    private WorkflowState lifecycleStatus;
    private String assignedStationId;
    private String assignedTechnicianId;
    private LocalDateTime scheduledStartTime;
    private LocalDateTime scheduledEndTime;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String resultNotes;

    public RecoveryJob() {
    }

    public RecoveryJob(String id, String title, String sourceDeviceId, String requiredOperation,
                       List<String> recoverableComponentIds, double priorityScore, UrgencyLevel urgency,
                       double estimatedRecoveryValue, int processingDurationMinutes, int requiredCapacityUnits,
                       TechnicianSkill requiredTechnicianSkill, WorkflowState lifecycleStatus,
                       String assignedStationId, String assignedTechnicianId,
                       LocalDateTime scheduledStartTime, LocalDateTime scheduledEndTime,
                       LocalDateTime createdAt, LocalDateTime completedAt, String resultNotes) {
        this.id = id;
        this.title = title;
        this.sourceDeviceId = sourceDeviceId;
        this.requiredOperation = requiredOperation;
        if (recoverableComponentIds != null) {
            this.recoverableComponentIds = new ArrayList<>(recoverableComponentIds);
        }
        this.priorityScore = priorityScore;
        this.urgency = urgency;
        this.estimatedRecoveryValue = estimatedRecoveryValue;
        this.processingDurationMinutes = processingDurationMinutes;
        this.requiredCapacityUnits = requiredCapacityUnits;
        this.requiredTechnicianSkill = requiredTechnicianSkill;
        this.lifecycleStatus = lifecycleStatus;
        this.assignedStationId = assignedStationId;
        this.assignedTechnicianId = assignedTechnicianId;
        this.scheduledStartTime = scheduledStartTime;
        this.scheduledEndTime = scheduledEndTime;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.resultNotes = resultNotes;
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

    public String getSourceDeviceId() {
        return sourceDeviceId;
    }

    public void setSourceDeviceId(String sourceDeviceId) {
        this.sourceDeviceId = sourceDeviceId;
    }

    public String getRequiredOperation() {
        return requiredOperation;
    }

    public void setRequiredOperation(String requiredOperation) {
        this.requiredOperation = requiredOperation;
    }

    public List<String> getRecoverableComponentIds() {
        return recoverableComponentIds;
    }

    public void setRecoverableComponentIds(List<String> recoverableComponentIds) {
        this.recoverableComponentIds = recoverableComponentIds != null ? new ArrayList<>(recoverableComponentIds) : new ArrayList<>();
    }

    public double getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(double priorityScore) {
        this.priorityScore = priorityScore;
    }

    public UrgencyLevel getUrgency() {
        return urgency;
    }

    public void setUrgency(UrgencyLevel urgency) {
        this.urgency = urgency;
    }

    public double getEstimatedRecoveryValue() {
        return estimatedRecoveryValue;
    }

    public void setEstimatedRecoveryValue(double estimatedRecoveryValue) {
        this.estimatedRecoveryValue = estimatedRecoveryValue;
    }

    public int getProcessingDurationMinutes() {
        return processingDurationMinutes;
    }

    public void setProcessingDurationMinutes(int processingDurationMinutes) {
        this.processingDurationMinutes = processingDurationMinutes;
    }

    public int getRequiredCapacityUnits() {
        return requiredCapacityUnits;
    }

    public void setRequiredCapacityUnits(int requiredCapacityUnits) {
        this.requiredCapacityUnits = requiredCapacityUnits;
    }

    public TechnicianSkill getRequiredTechnicianSkill() {
        return requiredTechnicianSkill;
    }

    public void setRequiredTechnicianSkill(TechnicianSkill requiredTechnicianSkill) {
        this.requiredTechnicianSkill = requiredTechnicianSkill;
    }

    public WorkflowState getLifecycleStatus() {
        return lifecycleStatus;
    }

    public void setLifecycleStatus(WorkflowState lifecycleStatus) {
        this.lifecycleStatus = lifecycleStatus;
    }

    public String getAssignedStationId() {
        return assignedStationId;
    }

    public void setAssignedStationId(String assignedStationId) {
        this.assignedStationId = assignedStationId;
    }

    public String getAssignedTechnicianId() {
        return assignedTechnicianId;
    }

    public void setAssignedTechnicianId(String assignedTechnicianId) {
        this.assignedTechnicianId = assignedTechnicianId;
    }

    public LocalDateTime getScheduledStartTime() {
        return scheduledStartTime;
    }

    public void setScheduledStartTime(LocalDateTime scheduledStartTime) {
        this.scheduledStartTime = scheduledStartTime;
    }

    public LocalDateTime getScheduledEndTime() {
        return scheduledEndTime;
    }

    public void setScheduledEndTime(LocalDateTime scheduledEndTime) {
        this.scheduledEndTime = scheduledEndTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getResultNotes() {
        return resultNotes;
    }

    public void setResultNotes(String resultNotes) {
        this.resultNotes = resultNotes;
    }
}
