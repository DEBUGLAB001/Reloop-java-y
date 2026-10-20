package com.debuglab.reloop.model;

import java.time.LocalDateTime;

public class AuditActivity {
    private String id;
    private LocalDateTime timestamp;
    private String action;
    private String entityType;
    private String entityId;
    private String details;

    public AuditActivity() {
    }

    public AuditActivity(String id, LocalDateTime timestamp, String action, String entityType, String entityId, String details) {
        this.id = id;
        this.timestamp = timestamp;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
