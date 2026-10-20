package com.debuglab.reloop.model;

import java.util.HashSet;
import java.util.Set;

public class ProcessingStation {
    private String id;
    private String name;
    private Set<String> supportedOperations = new HashSet<>();
    private int maxCapacityUnits;
    private int allocatedCapacityUnits;
    private String operationalStatus; // ONLINE, MAINTENANCE, OFFLINE

    public ProcessingStation() {
    }

    public ProcessingStation(String id, String name, Set<String> supportedOperations,
                             int maxCapacityUnits, int allocatedCapacityUnits, String operationalStatus) {
        this.id = id;
        this.name = name;
        if (supportedOperations != null) {
            this.supportedOperations = new HashSet<>(supportedOperations);
        }
        this.maxCapacityUnits = maxCapacityUnits;
        this.allocatedCapacityUnits = allocatedCapacityUnits;
        this.operationalStatus = operationalStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getSupportedOperations() {
        return supportedOperations;
    }

    public void setSupportedOperations(Set<String> supportedOperations) {
        this.supportedOperations = supportedOperations != null ? new HashSet<>(supportedOperations) : new HashSet<>();
    }

    public int getMaxCapacityUnits() {
        return maxCapacityUnits;
    }

    public void setMaxCapacityUnits(int maxCapacityUnits) {
        this.maxCapacityUnits = maxCapacityUnits;
    }

    public int getAllocatedCapacityUnits() {
        return allocatedCapacityUnits;
    }

    public void setAllocatedCapacityUnits(int allocatedCapacityUnits) {
        this.allocatedCapacityUnits = allocatedCapacityUnits;
    }

    public String getOperationalStatus() {
        return operationalStatus;
    }

    public void setOperationalStatus(String operationalStatus) {
        this.operationalStatus = operationalStatus;
    }

    public int getAvailableCapacity() {
        return Math.max(0, maxCapacityUnits - allocatedCapacityUnits);
    }

    public boolean supportsOperation(String operation) {
        return supportedOperations.contains(operation);
    }

    public boolean isOperational() {
        return "ONLINE".equalsIgnoreCase(operationalStatus);
    }

    public void allocateCapacity(int units) {
        this.allocatedCapacityUnits += units;
    }

    public void releaseCapacity(int units) {
        this.allocatedCapacityUnits = Math.max(0, this.allocatedCapacityUnits - units);
    }
}
