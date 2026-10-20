package com.debuglab.reloop.model;

public class CompatibilityResult {
    private boolean compatible;
    private int score;
    private String reason;
    private String candidateComponentId;
    private String requirementId;

    public CompatibilityResult() {
    }

    public CompatibilityResult(boolean compatible, int score, String reason, String candidateComponentId, String requirementId) {
        this.compatible = compatible;
        this.score = score;
        this.reason = reason;
        this.candidateComponentId = candidateComponentId;
        this.requirementId = requirementId;
    }

    public static CompatibilityResult match(int score, String reason, String candidateComponentId, String requirementId) {
        return new CompatibilityResult(true, score, reason, candidateComponentId, requirementId);
    }

    public static CompatibilityResult reject(String reason, String candidateComponentId, String requirementId) {
        return new CompatibilityResult(false, 0, reason, candidateComponentId, requirementId);
    }

    public boolean isCompatible() {
        return compatible;
    }

    public void setCompatible(boolean compatible) {
        this.compatible = compatible;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getCandidateComponentId() {
        return candidateComponentId;
    }

    public void setCandidateComponentId(String candidateComponentId) {
        this.candidateComponentId = candidateComponentId;
    }

    public String getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(String requirementId) {
        this.requirementId = requirementId;
    }
}
