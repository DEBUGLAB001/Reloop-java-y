package com.debuglab.reloop.controller;

import com.debuglab.reloop.model.AuditActivity;
import com.debuglab.reloop.model.Component;
import com.debuglab.reloop.model.ComponentAvailability;
import com.debuglab.reloop.model.Device;
import com.debuglab.reloop.model.ProcessingStation;
import com.debuglab.reloop.model.RecoveryJob;
import com.debuglab.reloop.model.Technician;
import com.debuglab.reloop.model.WorkflowState;
import com.debuglab.reloop.repository.ActivityRepository;
import com.debuglab.reloop.repository.ComponentRepository;
import com.debuglab.reloop.repository.DeviceRepository;
import com.debuglab.reloop.repository.ProcessingStationRepository;
import com.debuglab.reloop.repository.RecoveryJobRepository;
import com.debuglab.reloop.repository.TechnicianRepository;
import com.debuglab.reloop.service.ScenarioDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OperationsOverviewController {

    private final DeviceRepository deviceRepository;
    private final ComponentRepository componentRepository;
    private final RecoveryJobRepository jobRepository;
    private final ProcessingStationRepository stationRepository;
    private final TechnicianRepository technicianRepository;
    private final ActivityRepository activityRepository;
    private final ScenarioDataService scenarioDataService;

    public OperationsOverviewController(DeviceRepository deviceRepository,
                                       ComponentRepository componentRepository,
                                       RecoveryJobRepository jobRepository,
                                       ProcessingStationRepository stationRepository,
                                       TechnicianRepository technicianRepository,
                                       ActivityRepository activityRepository,
                                       ScenarioDataService scenarioDataService) {
        this.deviceRepository = deviceRepository;
        this.componentRepository = componentRepository;
        this.jobRepository = jobRepository;
        this.stationRepository = stationRepository;
        this.technicianRepository = technicianRepository;
        this.activityRepository = activityRepository;
        this.scenarioDataService = scenarioDataService;
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOperationsOverview() {
        List<Device> devices = deviceRepository.findAll();
        List<Component> components = componentRepository.findAll();
        List<RecoveryJob> jobs = jobRepository.findAll();
        List<ProcessingStation> stations = stationRepository.findAll();
        List<Technician> technicians = technicianRepository.findAll();
        List<AuditActivity> activities = activityRepository.findAll();

        long incomingDevicesCount = devices.stream()
                .filter(d -> d.getWorkflowStatus() == WorkflowState.RECEIVED || d.getWorkflowStatus() == WorkflowState.INSPECTING)
                .count();

        long readyForRecoveryCount = devices.stream()
                .filter(d -> d.getWorkflowStatus() == WorkflowState.READY_FOR_RECOVERY)
                .count();

        long availableComponentsCount = components.stream()
                .filter(c -> c.getAvailabilityStatus() == ComponentAvailability.AVAILABLE && c.getReservedForJobId() == null)
                .count();

        long reservedComponentsCount = components.stream()
                .filter(c -> c.getAvailabilityStatus() == ComponentAvailability.RESERVED || c.getReservedForJobId() != null)
                .count();

        long pendingJobsCount = jobs.stream()
                .filter(j -> j.getLifecycleStatus() == WorkflowState.READY_FOR_RECOVERY)
                .count();

        long scheduledJobsCount = jobs.stream()
                .filter(j -> j.getLifecycleStatus() == WorkflowState.SCHEDULED || j.getLifecycleStatus() == WorkflowState.IN_RECOVERY)
                .count();

        long completedJobsCount = jobs.stream()
                .filter(j -> j.getLifecycleStatus() == WorkflowState.COMPLETED)
                .count();

        Map<String, Object> overview = new HashMap<>();
        overview.put("incomingDevicesCount", incomingDevicesCount);
        overview.put("readyForRecoveryDevicesCount", readyForRecoveryCount);
        overview.put("totalComponentsCount", components.size());
        overview.put("availableComponentsCount", availableComponentsCount);
        overview.put("reservedComponentsCount", reservedComponentsCount);
        overview.put("pendingJobsCount", pendingJobsCount);
        overview.put("scheduledJobsCount", scheduledJobsCount);
        overview.put("completedJobsCount", completedJobsCount);
        overview.put("stations", stations);
        overview.put("technicians", technicians);
        overview.put("recentActivities", activities.stream().limit(10).toList());

        return ResponseEntity.ok(overview);
    }

    @GetMapping("/activities")
    public ResponseEntity<List<AuditActivity>> getActivities() {
        return ResponseEntity.ok(activityRepository.findAll());
    }

    @PostMapping("/scenario/reset")
    public ResponseEntity<Map<String, String>> resetScenario() {
        scenarioDataService.resetScenario();
        return ResponseEntity.ok(Map.of("message", "Demonstration scenario successfully reset to baseline."));
    }
}
