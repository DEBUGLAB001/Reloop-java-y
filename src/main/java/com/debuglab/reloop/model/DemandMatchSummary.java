package com.debuglab.reloop.model;

import java.util.ArrayList;
import java.util.List;

public class DemandMatchSummary {
    public static final DemandMatchSummary EMPTY = new DemandMatchSummary(0.0, 0, List.of(), List.of());

    private double demandScore;
    private int matchedRequirementsCount;
    private List<String> matchedRequirementIds = new ArrayList<>();
    private List<String> matchedComponentIds = new ArrayList<>();

    public DemandMatchSummary() {
    }

    public DemandMatchSummary(double demandScore, int matchedRequirementsCount,
                              List<String> matchedRequirementIds, List<String> matchedComponentIds) {
        this.demandScore = demandScore;
        this.matchedRequirementsCount = matchedRequirementsCount;
        if (matchedRequirementIds != null) {
            this.matchedRequirementIds = new ArrayList<>(matchedRequirementIds);
        }
        if (matchedComponentIds != null) {
            this.matchedComponentIds = new ArrayList<>(matchedComponentIds);
        }
    }

    public double getDemandScore() {
        return demandScore;
    }

    public void setDemandScore(double demandScore) {
        this.demandScore = demandScore;
    }

    public int getMatchedRequirementsCount() {
        return matchedRequirementsCount;
    }

    public void setMatchedRequirementsCount(int matchedRequirementsCount) {
        this.matchedRequirementsCount = matchedRequirementsCount;
    }

    public List<String> getMatchedRequirementIds() {
        return matchedRequirementIds;
    }

    public void setMatchedRequirementIds(List<String> matchedRequirementIds) {
        this.matchedRequirementIds = matchedRequirementIds != null ? new ArrayList<>(matchedRequirementIds) : new ArrayList<>();
    }

    public List<String> getMatchedComponentIds() {
        return matchedComponentIds;
    }

    public void setMatchedComponentIds(List<String> matchedComponentIds) {
        this.matchedComponentIds = matchedComponentIds != null ? new ArrayList<>(matchedComponentIds) : new ArrayList<>();
    }
}
