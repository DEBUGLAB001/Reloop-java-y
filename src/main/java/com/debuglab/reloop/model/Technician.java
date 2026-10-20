package com.debuglab.reloop.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Technician {
    private String id;
    private String name;
    private Set<TechnicianSkill> skills = new HashSet<>();
    private LocalTime shiftStart;
    private LocalTime shiftEnd;
    private List<ScheduledAssignment> scheduledAssignments = new ArrayList<>();

    public Technician() {
    }

    public Technician(String id, String name, Set<TechnicianSkill> skills,
                      LocalTime shiftStart, LocalTime shiftEnd,
                      List<ScheduledAssignment> scheduledAssignments) {
        this.id = id;
        this.name = name;
        if (skills != null) {
            this.skills = new HashSet<>(skills);
        }
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
        if (scheduledAssignments != null) {
            this.scheduledAssignments = new ArrayList<>(scheduledAssignments);
        }
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

    public Set<TechnicianSkill> getSkills() {
        return skills;
    }

    public void setSkills(Set<TechnicianSkill> skills) {
        this.skills = skills != null ? new HashSet<>(skills) : new HashSet<>();
    }

    public LocalTime getShiftStart() {
        return shiftStart;
    }

    public void setShiftStart(LocalTime shiftStart) {
        this.shiftStart = shiftStart;
    }

    public LocalTime getShiftEnd() {
        return shiftEnd;
    }

    public void setShiftEnd(LocalTime shiftEnd) {
        this.shiftEnd = shiftEnd;
    }

    public List<ScheduledAssignment> getScheduledAssignments() {
        return scheduledAssignments;
    }

    public void setScheduledAssignments(List<ScheduledAssignment> scheduledAssignments) {
        this.scheduledAssignments = scheduledAssignments != null ? new ArrayList<>(scheduledAssignments) : new ArrayList<>();
    }

    public boolean hasSkill(TechnicianSkill skill) {
        return skills.contains(skill);
    }

    public void addAssignment(ScheduledAssignment assignment) {
        this.scheduledAssignments.add(assignment);
    }

    public boolean removeAssignment(String jobId) {
        return this.scheduledAssignments.removeIf(a -> jobId.equals(a.getJobId()));
    }
}
