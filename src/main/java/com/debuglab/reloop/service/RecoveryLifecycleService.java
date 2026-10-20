package com.debuglab.reloop.service;

import com.debuglab.reloop.model.AuditActivity;
import com.debuglab.reloop.model.Device;
import com.debuglab.reloop.model.RecoveryJob;
import com.debuglab.reloop.model.WorkflowState;
import com.debuglab.reloop.repository.ActivityRepository;
import com.debuglab.reloop.repository.DeviceRepository;
import com.debuglab.reloop.repository.RecoveryJobRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class RecoveryLifecycleService {

    private static final Map<WorkflowState, Set<WorkflowState>> ALLOWED_TRANSITIONS = Map.of(
            WorkflowState.RECEIVED, Set.of(WorkflowState.INSPECTING, WorkflowState.REJECTED),
            WorkflowState.INSPECTING, Set.of(WorkflowState.READY_FOR_RECOVERY, WorkflowState.REJECTED),
            WorkflowState.READY_FOR_RECOVERY, Set.of(WorkflowState.SCHEDULED, WorkflowState.REJECTED, WorkflowState.COMPLETED),
            WorkflowState.SCHEDULED, Set.of(WorkflowState.IN_RECOVERY, WorkflowState.READY_FOR_RECOVERY),
            WorkflowState.IN_RECOVERY, Set.of(WorkflowState.RECOVERED, WorkflowState.READY_FOR_RECOVERY),
            WorkflowState.RECOVERED, Set.of(WorkflowState.COMPLETED),
            WorkflowState.COMPLETED, Set.of(),
            WorkflowState.REJECTED, Set.of()
    );

    private final RecoveryJobRepository jobRepository;
    private final DeviceRepository deviceRepository;
    private final ActivityRepository activityRepository;

    public RecoveryLifecycleService(RecoveryJobRepository jobRepository,
                                  DeviceRepository deviceRepository,
                                  ActivityRepository activityRepository) {
        this.jobRepository = jobRepository;
        this.deviceRepository = deviceRepository;
        this.activityRepository = activityRepository;
    }

    public boolean isValidTransition(WorkflowState from, WorkflowState to) {
        if (from == null || to == null) {
            return false;
        }
        Set<WorkflowState> targets = ALLOWED_TRANSITIONS.get(from);
        return targets != null && targets.contains(to);
    }

    public RecoveryJob transitionJob(String jobId, WorkflowState targetState, String reason) {
        Optional<RecoveryJob> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            throw new IllegalArgumentException("Recovery job not found: " + jobId);
        }
        RecoveryJob job = jobOpt.get();
        WorkflowState currentState = job.getLifecycleStatus();

        if (!isValidTransition(currentState, targetState)) {
            throw new IllegalStateException(
                    String.format("Invalid state transition from %s to %s for job %s",
                            currentState, targetState, jobId));
        }

        job.setLifecycleStatus(targetState);
        if (targetState == WorkflowState.COMPLETED) {
            job.setCompletedAt(LocalDateTime.now());
            job.setResultNotes(reason != null ? reason : "Recovery successfully completed");

            // Also update source device if all jobs completed
            if (job.getSourceDeviceId() != null) {
                deviceRepository.findById(job.getSourceDeviceId()).ifPresent(device -> {
                    device.setWorkflowStatus(WorkflowState.COMPLETED);
                    deviceRepository.save(device);
                });
            }
        } else if (targetState == WorkflowState.REJECTED) {
            job.setResultNotes(reason != null ? reason : "Job rejected during processing");
        }

        jobRepository.save(job);

        activityRepository.save(new AuditActivity(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                "WORKFLOW_TRANSITION",
                "RecoveryJob",
                jobId,
                String.format("Transitioned from %s to %s. Notes: %s", currentState, targetState, reason)
        ));

        return job;
    }

    public Device transitionDevice(String deviceId, WorkflowState targetState, String notes) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) {
            throw new IllegalArgumentException("Device not found: " + deviceId);
        }
        Device device = deviceOpt.get();
        WorkflowState currentState = device.getWorkflowStatus();

        if (!isValidTransition(currentState, targetState)) {
            throw new IllegalStateException(
                    String.format("Invalid state transition from %s to %s for device %s",
                            currentState, targetState, deviceId));
        }

        device.setWorkflowStatus(targetState);
        if (notes != null && !notes.isBlank()) {
            device.setInspectionNotes(notes);
        }
        deviceRepository.save(device);

        activityRepository.save(new AuditActivity(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                "DEVICE_INSPECTION_UPDATE",
                "Device",
                deviceId,
                String.format("Device %s transitioned from %s to %s", device.getModel(), currentState, targetState)
        ));

        return device;
    }
}
