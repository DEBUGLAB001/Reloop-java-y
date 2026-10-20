package com.debuglab.reloop.service;

import com.debuglab.reloop.model.AuditActivity;
import com.debuglab.reloop.model.Component;
import com.debuglab.reloop.model.Device;
import com.debuglab.reloop.model.RecoveryJob;
import com.debuglab.reloop.model.TechnicianSkill;
import com.debuglab.reloop.model.UrgencyLevel;
import com.debuglab.reloop.model.WorkflowState;
import com.debuglab.reloop.repository.ActivityRepository;
import com.debuglab.reloop.repository.ComponentRepository;
import com.debuglab.reloop.repository.DeviceRepository;
import com.debuglab.reloop.repository.RecoveryJobRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DeviceIntakeService {

    private final DeviceRepository deviceRepository;
    private final ComponentRepository componentRepository;
    private final RecoveryJobRepository jobRepository;
    private final ActivityRepository activityRepository;

    public DeviceIntakeService(DeviceRepository deviceRepository,
                               ComponentRepository componentRepository,
                               RecoveryJobRepository jobRepository,
                               ActivityRepository activityRepository) {
        this.deviceRepository = deviceRepository;
        this.componentRepository = componentRepository;
        this.jobRepository = jobRepository;
        this.activityRepository = activityRepository;
    }

    public Device completeInspection(String deviceId, String condition, List<Component> components, String notes) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) {
            throw new IllegalArgumentException("Device not found: " + deviceId);
        }
        Device device = deviceOpt.get();

        device.setCondition(condition);
        device.setInspectionNotes(notes);
        device.setWorkflowStatus(WorkflowState.READY_FOR_RECOVERY);

        List<String> componentIds = new ArrayList<>();
        if (components != null) {
            for (Component comp : components) {
                if (comp.getId() == null || comp.getId().isBlank()) {
                    comp.setId("CMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                }
                comp.setSourceDeviceId(deviceId);
                componentRepository.save(comp);
                componentIds.add(comp.getId());
            }
        }
        device.setRecoverableComponentIds(componentIds);
        deviceRepository.save(device);

        // Auto-create recovery job for inspection workflow
        String jobId = "JOB-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        RecoveryJob job = new RecoveryJob(
                jobId,
                "Recovery for " + device.getModel(),
                deviceId,
                "PRECISION_TEARDOWN",
                componentIds,
                50.0,
                UrgencyLevel.HIGH,
                200.0,
                60,
                30,
                TechnicianSkill.DISASSEMBLY,
                WorkflowState.READY_FOR_RECOVERY,
                null,
                null,
                null,
                null,
                LocalDateTime.now(),
                null,
                "Generated from intake inspection"
        );
        jobRepository.save(job);

        activityRepository.save(new AuditActivity(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                "DEVICE_INSPECTED",
                "Device",
                deviceId,
                String.format("Inspection completed for %s. Condition: %s, Discovered %d components",
                        device.getModel(), condition, componentIds.size())
        ));

        return device;
    }
}
