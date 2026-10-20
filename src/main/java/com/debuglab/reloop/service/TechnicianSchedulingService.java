package com.debuglab.reloop.service;

import com.debuglab.reloop.model.RecoveryJob;
import com.debuglab.reloop.model.ScheduledAssignment;
import com.debuglab.reloop.model.SchedulingResult;
import com.debuglab.reloop.model.Technician;
import com.debuglab.reloop.model.WorkflowState;
import com.debuglab.reloop.repository.RecoveryJobRepository;
import com.debuglab.reloop.repository.TechnicianRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class TechnicianSchedulingService {

    private final TechnicianRepository technicianRepository;
    private final RecoveryJobRepository jobRepository;

    public TechnicianSchedulingService(TechnicianRepository technicianRepository,
                                       RecoveryJobRepository jobRepository) {
        this.technicianRepository = technicianRepository;
        this.jobRepository = jobRepository;
    }

    public boolean hasSchedulingConflict(Technician technician, LocalDateTime candidateStart, LocalDateTime candidateEnd) {
        if (technician == null || technician.getScheduledAssignments() == null) {
            return false;
        }

        return technician.getScheduledAssignments().stream()
                .anyMatch(existing -> candidateStart.isAfter(existing.getStartTime()) && candidateEnd.isBefore(existing.getEndTime()));
    }

    public boolean isWithinShift(Technician technician, LocalDateTime startTime, LocalDateTime endTime) {
        LocalTime start = startTime.toLocalTime();
        LocalTime end = endTime.toLocalTime();
        return !start.isBefore(technician.getShiftStart()) && !end.isAfter(technician.getShiftEnd());
    }

    public Optional<Technician> findAvailableTechnician(RecoveryJob job, LocalDateTime startTime) {
        LocalDateTime endTime = startTime.plusMinutes(job.getProcessingDurationMinutes());
        List<Technician> technicians = technicianRepository.findAll();

        return technicians.stream()
                .filter(t -> t.hasSkill(job.getRequiredTechnicianSkill()))
                .filter(t -> isWithinShift(t, startTime, endTime))
                .filter(t -> !hasSchedulingConflict(t, startTime, endTime))
                .min(Comparator.comparingInt(t -> t.getScheduledAssignments().size()));
    }

    public SchedulingResult scheduleJob(String jobId, String technicianId, LocalDateTime startTime) {
        Optional<RecoveryJob> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            return SchedulingResult.failure(jobId, "Job not found: " + jobId);
        }
        RecoveryJob job = jobOpt.get();

        Optional<Technician> techOpt = technicianRepository.findById(technicianId);
        if (techOpt.isEmpty()) {
            return SchedulingResult.failure(jobId, "Technician not found: " + technicianId);
        }
        Technician technician = techOpt.get();

        if (!technician.hasSkill(job.getRequiredTechnicianSkill())) {
            return SchedulingResult.failure(jobId,
                    String.format("Technician %s lacks required skill %s",
                            technician.getName(), job.getRequiredTechnicianSkill()));
        }

        LocalDateTime endTime = startTime.plusMinutes(job.getProcessingDurationMinutes());

        if (!isWithinShift(technician, startTime, endTime)) {
            return SchedulingResult.failure(jobId,
                    String.format("Scheduled window %s - %s falls outside technician shift (%s - %s)",
                            startTime.toLocalTime(), endTime.toLocalTime(),
                            technician.getShiftStart(), technician.getShiftEnd()));
        }

        if (hasSchedulingConflict(technician, startTime, endTime)) {
            return SchedulingResult.failure(jobId,
                    String.format("Technician %s has a scheduling conflict during %s - %s",
                            technician.getName(), startTime.toLocalTime(), endTime.toLocalTime()));
        }

        ScheduledAssignment assignment = new ScheduledAssignment(job.getId(), startTime, endTime, job.getRequiredOperation());
        technician.addAssignment(assignment);
        technicianRepository.save(technician);

        job.setAssignedTechnicianId(technician.getId());
        job.setScheduledStartTime(startTime);
        job.setScheduledEndTime(endTime);
        job.setLifecycleStatus(WorkflowState.SCHEDULED);
        jobRepository.save(job);

        return SchedulingResult.success(technician.getId(), job.getId(), startTime, endTime,
                String.format("Job %s successfully scheduled with %s (%s - %s)",
                        job.getTitle(), technician.getName(), startTime.toLocalTime(), endTime.toLocalTime()));
    }
}
