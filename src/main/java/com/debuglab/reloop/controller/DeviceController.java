package com.debuglab.reloop.controller;

import com.debuglab.reloop.model.Component;
import com.debuglab.reloop.model.Device;
import com.debuglab.reloop.model.WorkflowState;
import com.debuglab.reloop.repository.DeviceRepository;
import com.debuglab.reloop.service.DeviceIntakeService;
import com.debuglab.reloop.service.RecoveryLifecycleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceRepository deviceRepository;
    private final DeviceIntakeService intakeService;
    private final RecoveryLifecycleService lifecycleService;

    public DeviceController(DeviceRepository deviceRepository,
                            DeviceIntakeService intakeService,
                            RecoveryLifecycleService lifecycleService) {
        this.deviceRepository = deviceRepository;
        this.intakeService = intakeService;
        this.lifecycleService = lifecycleService;
    }

    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        return ResponseEntity.ok(deviceRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable String id) {
        return deviceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public static class InspectionDto {
        private String condition;
        private String notes;
        private List<Component> components;

        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public List<Component> getComponents() { return components; }
        public void setComponents(List<Component> components) { this.components = components; }
    }

    @PostMapping("/{id}/inspect")
    public ResponseEntity<?> completeInspection(@PathVariable String id, @RequestBody InspectionDto dto) {
        try {
            Device inspected = intakeService.completeInspection(id, dto.getCondition(), dto.getComponents(), dto.getNotes());
            return ResponseEntity.ok(inspected);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    public static class TransitionDto {
        private String targetState;
        private String notes;

        public String getTargetState() { return targetState; }
        public void setTargetState(String targetState) { this.targetState = targetState; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    @PostMapping("/{id}/transition")
    public ResponseEntity<?> transitionDevice(@PathVariable String id, @RequestBody TransitionDto dto) {
        try {
            WorkflowState state = WorkflowState.valueOf(dto.getTargetState());
            Device updated = lifecycleService.transitionDevice(id, state, dto.getNotes());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
