package com.debuglab.reloop.model;

public enum WorkflowState {
    RECEIVED,
    INSPECTING,
    READY_FOR_RECOVERY,
    SCHEDULED,
    IN_RECOVERY,
    RECOVERED,
    REJECTED,
    COMPLETED
}
