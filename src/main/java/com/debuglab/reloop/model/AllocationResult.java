package com.debuglab.reloop.model;

public class AllocationResult {
    private boolean successful;
    private String stationId;
    private String jobId;
    private int allocatedCapacity;
    private String message;

    public AllocationResult() {
    }

    public AllocationResult(boolean successful, String stationId, String jobId, int allocatedCapacity, String message) {
        this.successful = successful;
        this.stationId = stationId;
        this.jobId = jobId;
        this.allocatedCapacity = allocatedCapacity;
        this.message = message;
    }

    public static AllocationResult success(String stationId, String jobId, int allocatedCapacity, String message) {
        return new AllocationResult(true, stationId, jobId, allocatedCapacity, message);
    }

    public static AllocationResult failure(String jobId, String message) {
        return new AllocationResult(false, null, jobId, 0, message);
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public int getAllocatedCapacity() {
        return allocatedCapacity;
    }

    public void setAllocatedCapacity(int allocatedCapacity) {
        this.allocatedCapacity = allocatedCapacity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
