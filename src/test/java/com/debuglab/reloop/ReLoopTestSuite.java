package com.debuglab.reloop;

import com.debuglab.reloop.model.AllocationResult;
import com.debuglab.reloop.model.CompatibilityResult;
import com.debuglab.reloop.model.Component;
import com.debuglab.reloop.model.ComponentAvailability;
import com.debuglab.reloop.model.ComponentRequirement;
import com.debuglab.reloop.model.ConditionGrade;
import com.debuglab.reloop.model.ProcessingStation;
import com.debuglab.reloop.model.RecoveryJob;
import com.debuglab.reloop.model.Technician;
import com.debuglab.reloop.model.WorkflowState;
import com.debuglab.reloop.repository.ActivityRepository;
import com.debuglab.reloop.repository.ComponentRepository;
import com.debuglab.reloop.repository.DeviceRepository;
import com.debuglab.reloop.repository.ProcessingStationRepository;
import com.debuglab.reloop.repository.RecoveryJobRepository;
import com.debuglab.reloop.repository.RequirementRepository;
import com.debuglab.reloop.repository.TechnicianRepository;
import com.debuglab.reloop.service.BatchAllocationService;
import com.debuglab.reloop.service.ComponentMatchingService;
import com.debuglab.reloop.service.RecoveryLifecycleService;
import com.debuglab.reloop.service.RecoveryPriorityService;
import com.debuglab.reloop.service.RecoveryReservationService;
import com.debuglab.reloop.service.ScenarioDataService;
import com.debuglab.reloop.service.TechnicianSchedulingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ReLoopTestSuite {

    private final DeviceRepository deviceRepository = new DeviceRepository();
    private final ComponentRepository componentRepository = new ComponentRepository();
    private final RequirementRepository requirementRepository = new RequirementRepository();
    private final RecoveryJobRepository jobRepository = new RecoveryJobRepository();
    private final ProcessingStationRepository stationRepository = new ProcessingStationRepository();
    private final TechnicianRepository technicianRepository = new TechnicianRepository();
    private final ActivityRepository activityRepository = new ActivityRepository();

    private final ScenarioDataService scenarioDataService = new ScenarioDataService(
            deviceRepository, componentRepository, requirementRepository,
            jobRepository, stationRepository, technicianRepository, activityRepository
    );

    private final ComponentMatchingService matchingService = new ComponentMatchingService(
            componentRepository, requirementRepository
    );

    private final RecoveryPriorityService priorityService = new RecoveryPriorityService(
            jobRepository, matchingService
    );

    private final BatchAllocationService batchAllocationService = new BatchAllocationService(
            stationRepository, jobRepository
    );

    private final TechnicianSchedulingService schedulingService = new TechnicianSchedulingService(
            technicianRepository, jobRepository
    );

    private final RecoveryLifecycleService lifecycleService = new RecoveryLifecycleService(
            jobRepository, deviceRepository, activityRepository
    );

    private final RecoveryReservationService reservationService = new RecoveryReservationService(
            jobRepository, componentRepository, stationRepository, technicianRepository, activityRepository
    );

    private void reset() {
        scenarioDataService.resetScenario();
    }

    public void testComponentCompatibilityMatching() {
        reset();
        Component candidate = componentRepository.findById("CMP-101")
                .orElseThrow(() -> new AssertionError("Candidate component CMP-101 not found"));
        ComponentRequirement requirement = requirementRepository.findById("REQ-201")
                .orElseThrow(() -> new AssertionError("Requirement REQ-201 not found"));

        CompatibilityResult result = matchingService.evaluateCompatibility(candidate, requirement);

        if (!result.isCompatible()) {
            throw new AssertionError("Expected Grade A component (CMP-101) to satisfy Grade B requirement (REQ-201), but evaluation failed: " + result.getReason());
        }
    }

    public void testRecoveryJobPrioritization() {
        reset();
        List<RecoveryJob> prioritized = priorityService.prioritizeEligibleJobs();
        if (prioritized.isEmpty()) {
            throw new AssertionError("Prioritization queue returned empty");
        }

        RecoveryJob topJob = prioritized.get(0);
        if (!"JOB-101".equals(topJob.getId())) {
            throw new AssertionError(String.format(
                    "Expected high-demand critical recovery job JOB-101 to lead queue, but found %s with priority score %.1f (JOB-101 score: %.1f)",
                    topJob.getId(), topJob.getPriorityScore(),
                    prioritized.stream().filter(j -> "JOB-101".equals(j.getId())).map(RecoveryJob::getPriorityScore).findFirst().orElse(0.0)
            ));
        }
    }

    public void testBatchCapacityAllocation() {
        reset();
        ProcessingStation station = stationRepository.findById("STN-01")
                .orElseThrow(() -> new AssertionError("Station STN-01 not found"));
        RecoveryJob job = jobRepository.findById("JOB-104")
                .orElseThrow(() -> new AssertionError("Job JOB-104 not found"));

        // STN-01 has max 100, allocated 75 (25 available). JOB-104 requires 35 units.
        boolean canAccommodate = batchAllocationService.canStationAccommodate(station, job);
        if (canAccommodate) {
            throw new AssertionError(String.format(
                    "Station %s has only %d units remaining capacity, but allocator allowed job %s requiring %d units",
                    station.getName(), station.getAvailableCapacity(), job.getTitle(), job.getRequiredCapacityUnits()
            ));
        }

        AllocationResult result = batchAllocationService.allocateJobToStation(job.getId(), station.getId());
        if (result.isSuccessful()) {
            throw new AssertionError(String.format(
                    "Over-allocated station: allocation succeeded despite exceeding capacity limit (%d > %d)",
                    station.getAllocatedCapacityUnits(), station.getMaxCapacityUnits()
            ));
        }
    }

    public void testTechnicianSchedulingInterval() {
        reset();
        Technician technician = technicianRepository.findById("TECH-01")
                .orElseThrow(() -> new AssertionError("Technician TECH-01 not found"));

        LocalDate today = LocalDate.now();
        LocalDateTime candidateStart = LocalDateTime.of(today, LocalTime.of(10, 0));
        LocalDateTime candidateEnd = LocalDateTime.of(today, LocalTime.of(12, 0));

        // TECH-01 already has assignment 09:00 - 11:00. Candidate window 10:00 - 12:00 overlaps from 10:00 to 11:00.
        boolean hasConflict = schedulingService.hasSchedulingConflict(technician, candidateStart, candidateEnd);
        if (!hasConflict) {
            throw new AssertionError(String.format(
                    "Scheduling conflict not detected for %s: candidate window 10:00-12:00 overlaps with existing 09:00-11:00 assignment",
                    technician.getName()
            ));
        }
    }

    public void testRecoveryLifecycleTransitions() {
        reset();
        RecoveryJob job = jobRepository.findById("JOB-103")
                .orElseThrow(() -> new AssertionError("Job JOB-103 not found"));

        // JOB-103 is in READY_FOR_RECOVERY. Attempting direct transition to COMPLETED must be rejected.
        try {
            lifecycleService.transitionJob(job.getId(), WorkflowState.COMPLETED, "Premature completion bypass");
            throw new AssertionError("Invalid lifecycle transition succeeded: READY_FOR_RECOVERY transitioned directly to COMPLETED without undergoing recovery");
        } catch (IllegalStateException expected) {
            // Valid rejection behavior expected
        }
    }

    public void testReservationConsistencyOnCancellation() {
        reset();
        RecoveryJob job = jobRepository.findById("JOB-105")
                .orElseThrow(() -> new AssertionError("Job JOB-105 not found"));

        reservationService.cancelJob(job.getId());

        Component component = componentRepository.findById("CMP-107")
                .orElseThrow(() -> new AssertionError("Component CMP-107 not found"));

        if (component.getAvailabilityStatus() != ComponentAvailability.AVAILABLE) {
            throw new AssertionError(String.format(
                    "Component %s remained in status %s after cancelling job %s (expected AVAILABLE)",
                    component.getId(), component.getAvailabilityStatus(), job.getId()
            ));
        }
        if (!component.isAvailable()) {
            throw new AssertionError("Component is not marked available after job cancellation");
        }
    }
}
