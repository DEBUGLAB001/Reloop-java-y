package com.debuglab.reloop.model;

public class ComponentRequirement {
    private String id;
    private String title;
    private String requiredType;
    private int minimumCapacity;
    private String interfaceType;
    private ConditionGrade minimumConditionGrade;
    private String targetModelFamily;
    private UrgencyLevel urgency;
    private String fulfilledComponentId;
    private String status; // OPEN, MATCHED, FULFILLED

    public ComponentRequirement() {
    }

    public ComponentRequirement(String id, String title, String requiredType, int minimumCapacity,
                                String interfaceType, ConditionGrade minimumConditionGrade,
                                String targetModelFamily, UrgencyLevel urgency,
                                String fulfilledComponentId, String status) {
        this.id = id;
        this.title = title;
        this.requiredType = requiredType;
        this.minimumCapacity = minimumCapacity;
        this.interfaceType = interfaceType;
        this.minimumConditionGrade = minimumConditionGrade;
        this.targetModelFamily = targetModelFamily;
        this.urgency = urgency;
        this.fulfilledComponentId = fulfilledComponentId;
        this.status = status;
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

    public String getRequiredType() {
        return requiredType;
    }

    public void setRequiredType(String requiredType) {
        this.requiredType = requiredType;
    }

    public int getMinimumCapacity() {
        return minimumCapacity;
    }

    public void setMinimumCapacity(int minimumCapacity) {
        this.minimumCapacity = minimumCapacity;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public ConditionGrade getMinimumConditionGrade() {
        return minimumConditionGrade;
    }

    public void setMinimumConditionGrade(ConditionGrade minimumConditionGrade) {
        this.minimumConditionGrade = minimumConditionGrade;
    }

    public String getTargetModelFamily() {
        return targetModelFamily;
    }

    public void setTargetModelFamily(String targetModelFamily) {
        this.targetModelFamily = targetModelFamily;
    }

    public UrgencyLevel getUrgency() {
        return urgency;
    }

    public void setUrgency(UrgencyLevel urgency) {
        this.urgency = urgency;
    }

    public String getFulfilledComponentId() {
        return fulfilledComponentId;
    }

    public void setFulfilledComponentId(String fulfilledComponentId) {
        this.fulfilledComponentId = fulfilledComponentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
