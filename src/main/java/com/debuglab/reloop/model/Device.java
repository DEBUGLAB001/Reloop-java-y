package com.debuglab.reloop.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Device {
    private String id;
    private String model;
    private String category;
    private String condition;
    private WorkflowState workflowStatus;
    private LocalDateTime intakeTimestamp;
    private List<String> recoverableComponentIds = new ArrayList<>();
    private String inspectionNotes;

    public Device() {
    }

    public Device(String id, String model, String category, String condition,
                  WorkflowState workflowStatus, LocalDateTime intakeTimestamp,
                  List<String> recoverableComponentIds, String inspectionNotes) {
        this.id = id;
        this.model = model;
        this.category = category;
        this.condition = condition;
        this.workflowStatus = workflowStatus;
        this.intakeTimestamp = intakeTimestamp;
        if (recoverableComponentIds != null) {
            this.recoverableComponentIds = new ArrayList<>(recoverableComponentIds);
        }
        this.inspectionNotes = inspectionNotes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public WorkflowState getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(WorkflowState workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public LocalDateTime getIntakeTimestamp() {
        return intakeTimestamp;
    }

    public void setIntakeTimestamp(LocalDateTime intakeTimestamp) {
        this.intakeTimestamp = intakeTimestamp;
    }

    public List<String> getRecoverableComponentIds() {
        return recoverableComponentIds;
    }

    public void setRecoverableComponentIds(List<String> recoverableComponentIds) {
        this.recoverableComponentIds = recoverableComponentIds != null ? new ArrayList<>(recoverableComponentIds) : new ArrayList<>();
    }

    public String getInspectionNotes() {
        return inspectionNotes;
    }

    public void setInspectionNotes(String inspectionNotes) {
        this.inspectionNotes = inspectionNotes;
    }
}
