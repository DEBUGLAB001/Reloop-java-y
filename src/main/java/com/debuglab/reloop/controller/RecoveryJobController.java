package com.debuglab.reloop.controller;

import com.debuglab.reloop.model.RecoveryJob;
import com.debuglab.reloop.model.WorkflowState;
import com.debuglab.reloop.repository.RecoveryJobRepository;
import com.debuglab.reloop.service.RecoveryLifecycleService;
import com.debuglab.reloop.service.RecoveryPriorityService;
import com.debuglab.reloop.service.RecoveryReservationService;
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
@RequestMapping("/api/jobs")
public class RecoveryJobController {

    private final RecoveryJobRepository jobRepository;
    private final RecoveryPriorityService priorityService;
    private final RecoveryLifecycleService lifecycleService;
    private final RecoveryReservationService reservationService;

    public RecoveryJobController(RecoveryJobRepository jobRepository,
                                 RecoveryPriorityService priorityService,
                                 RecoveryLifecycleService lifecycleService,
                                 RecoveryReservationService reservationService) {
        this.jobRepository = jobRepository;
        this.priorityService = priorityService;
        this.lifecycleService = lifecycleService;
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<RecoveryJob>> getAllJobs() {
        return ResponseEntity.ok(jobRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecoveryJob> getJobById(@PathVariable String id) {
        return jobRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/prioritize")
    public ResponseEntity<List<RecoveryJob>> runPrioritization() {
        return ResponseEntity.ok(priorityService.prioritizeEligibleJobs());
    }

    public static class TransitionJobDto {
        private String targetState;
        private String reason;

        public String getTargetState() { return targetState; }
        public void setTargetState(String targetState) { this.targetState = targetState; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    @PostMapping("/{id}/transition")
    public ResponseEntity<?> transitionJob(@PathVariable String id, @RequestBody TransitionJobDto dto) {
        try {
            WorkflowState state = WorkflowState.valueOf(dto.getTargetState());
            RecoveryJob updated = lifecycleService.transitionJob(id, state, dto.getReason());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeJob(@PathVariable String id, @RequestBody(required = false) TransitionJobDto dto) {
        try {
            String reason = dto != null && dto.getReason() != null ? dto.getReason() : "Completed recovery process";
            RecoveryJob updated = lifecycleService.transitionJob(id, WorkflowState.COMPLETED, reason);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelJob(@PathVariable String id) {
        try {
            RecoveryJob cancelled = reservationService.cancelJob(id);
            return ResponseEntity.ok(cancelled);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
