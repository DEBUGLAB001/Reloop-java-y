package com.debuglab.reloop.service;

import com.debuglab.reloop.model.AuditActivity;
import com.debuglab.reloop.model.Component;
import com.debuglab.reloop.model.ComponentAvailability;
import com.debuglab.reloop.model.ProcessingStation;
import com.debuglab.reloop.model.RecoveryJob;
import com.debuglab.reloop.model.Technician;
import com.debuglab.reloop.model.WorkflowState;
import com.debuglab.reloop.repository.ActivityRepository;
import com.debuglab.reloop.repository.ComponentRepository;
import com.debuglab.reloop.repository.ProcessingStationRepository;
import com.debuglab.reloop.repository.RecoveryJobRepository;
import com.debuglab.reloop.repository.TechnicianRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RecoveryReservationService {

    private final RecoveryJobRepository jobRepository;
    private final ComponentRepository componentRepository;
    private final ProcessingStationRepository stationRepository;
    private final TechnicianRepository technicianRepository;
    private final ActivityRepository activityRepository;

    public RecoveryReservationService(RecoveryJobRepository jobRepository,
                                      ComponentRepository componentRepository,
                                      ProcessingStationRepository stationRepository,
                                      TechnicianRepository technicianRepository,
                                      ActivityRepository activityRepository) {
        this.jobRepository = jobRepository;
        this.componentRepository = componentRepository;
        this.stationRepository = stationRepository;
        this.technicianRepository = technicianRepository;
        this.activityRepository = activityRepository;
    }

    public Component reserveComponent(String componentId, String jobId) {
        Optional<Component> compOpt = componentRepository.findById(componentId);
        if (compOpt.isEmpty()) {
            throw new IllegalArgumentException("Component not found: " + componentId);
        }
        Component component = compOpt.get();
        if (!component.isAvailable()) {
            throw new IllegalStateException("Component is not available for reservation: " + componentId);
        }

        component.setAvailabilityStatus(ComponentAvailability.RESERVED);
        component.setReservedForJobId(jobId);
        componentRepository.save(component);

        activityRepository.save(new AuditActivity(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                "COMPONENT_RESERVED",
                "Component",
                componentId,
                "Reserved for recovery job " + jobId
        ));

        return component;
    }

    public RecoveryJob cancelJob(String jobId) {
        Optional<RecoveryJob> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            throw new IllegalArgumentException("Recovery job not found: " + jobId);
        }
        RecoveryJob job = jobOpt.get();

        if (job.getAssignedStationId() != null) {
            Optional<ProcessingStation> stationOpt = stationRepository.findById(job.getAssignedStationId());
            if (stationOpt.isPresent()) {
                ProcessingStation station = stationOpt.get();
                station.releaseCapacity(job.getRequiredCapacityUnits());
                stationRepository.save(station);
            }
            job.setAssignedStationId(null);
        }

        if (job.getAssignedTechnicianId() != null) {
            Optional<Technician> techOpt = technicianRepository.findById(job.getAssignedTechnicianId());
            if (techOpt.isPresent()) {
                Technician technician = techOpt.get();
                technician.removeAssignment(jobId);
                technicianRepository.save(technician);
            }
            job.setAssignedTechnicianId(null);
        }

        List<Component> reservedComponents = componentRepository.findByReservedForJobId(jobId);
        for (Component component : reservedComponents) {
            component.setReservedForJobId(null);
            componentRepository.save(component);
        }

        job.setLifecycleStatus(WorkflowState.REJECTED);
        job.setResultNotes("Cancelled by operations manager. Resources released.");
        jobRepository.save(job);

        activityRepository.save(new AuditActivity(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                "JOB_CANCELLED",
                "RecoveryJob",
                jobId,
                "Job cancelled and assigned resources released"
        ));

        return job;
    }
}
