# ReLoop

## Description
ReLoop is an e-waste recovery and refurbishment operations platform built with Java 17 and Spring Boot 3.2.5. The application manages device intake inspection, component harvesting, compatibility matching, priority queue planning, station capacity allocation, and technician scheduling. The backend entry point is `src/main/java/com/debuglab/reloop/ReLoopApplication.java`. Core domain logic resides in dedicated services including `ComponentMatchingService.java`, `RecoveryPriorityService.java`, `BatchAllocationService.java`, `TechnicianSchedulingService.java`, `RecoveryLifecycleService.java`, and `RecoveryReservationService.java`. The system uses thread-safe in-memory repositories pre-seeded with deterministic circular operations data managed by `ScenarioDataService.java`. A single-page dashboard in `src/main/resources/static/` communicates via REST endpoints rooted at `/api/`.

## Repository Structure
```
.
├── challenge.json                                      # Platform runtime configuration
├── pom.xml                                             # Maven build definition and dependencies
├── start.sh                                            # JVM startup and source reload watcher
└── src/
    └── main/
        ├── java/com/debuglab/reloop/
        │   ├── ReLoopApplication.java                 # Spring Boot application entry point
        │   ├── controller/                            # REST controllers for devices, jobs, stations
        │   │   ├── BatchAllocationController.java
        │   │   ├── ComponentController.java
        │   │   ├── DeviceController.java
        │   │   ├── OperationsOverviewController.java
        │   │   ├── RecoveryJobController.java
        │   │   └── TechnicianSchedulingController.java
        │   ├── model/                                 # Domain entities and workflow enumerations
        │   │   ├── AllocationResult.java
        │   │   ├── AuditActivity.java
        │   │   ├── CompatibilityResult.java
        │   │   ├── Component.java
        │   │   ├── ComponentAvailability.java
        │   │   ├── ComponentRequirement.java
        │   │   ├── ConditionGrade.java
        │   │   ├── DemandMatchSummary.java
        │   │   ├── Device.java
        │   │   ├── ProcessingStation.java
        │   │   ├── RecoveryJob.java
        │   │   ├── ScheduledAssignment.java
        │   │   ├── SchedulingResult.java
        │   │   ├── Technician.java
        │   │   ├── TechnicianSkill.java
        │   │   ├── UrgencyLevel.java
        │   │   └── WorkflowState.java
        │   ├── repository/                            # Thread-safe in-memory repositories
        │   │   ├── ActivityRepository.java
        │   │   ├── ComponentRepository.java
        │   │   ├── DeviceRepository.java
        │   │   ├── ProcessingStationRepository.java
        │   │   ├── RecoveryJobRepository.java
        │   │   ├── RequirementRepository.java
        │   │   └── TechnicianRepository.java
        │   └── service/                               # Business logic and operations services
        │       ├── BatchAllocationService.java
        │       ├── ComponentMatchingService.java
        │       ├── DeviceIntakeService.java
        │       ├── RecoveryLifecycleService.java
        │       ├── RecoveryPriorityService.java
        │       ├── RecoveryReservationService.java
        │       ├── ScenarioDataService.java
        │       └── TechnicianSchedulingService.java
        └── resources/
            ├── application.properties                 # Server port 3000 and logging config
            └── static/                                # Operations console frontend
                ├── css/styles.css
                ├── index.html
                └── js/app.js
```

## Bugs and Bug Locations

1. Inverted Condition Grade Comparison in Component Matching
- Bug location: `src/main/java/com/debuglab/reloop/service/ComponentMatchingService.java`, within `isGradeAcceptable(ConditionGrade, ConditionGrade)` around lines 78–83.
- How to observe it: Evaluate candidate component `CMP-101` (Grade A SSD) against requirement `REQ-201` (minimum Grade B) via `POST /api/requirements/REQ-201/match` or the Component Matching tab.
- Failure: Grade A component is rejected with "Condition grade A does not meet minimum required grade B". In Java enum ordering, Grade A has ordinal 0 and Grade B has ordinal 1, so `candidateGrade.ordinal() >= minimumGrade.ordinal()` evaluates to `0 >= 1` (`false`).
- Expected: Grade A components must satisfy Grade B minimum requirements (`candidateGrade.ordinal() <= minimumGrade.ordinal()`).

2. Inverted Duration Efficiency Penalty in Job Prioritization
- Bug location: `src/main/java/com/debuglab/reloop/service/RecoveryPriorityService.java`, within `calculateJobPriorityScore(RecoveryJob)` around lines 28–36.
- How to observe it: Trigger `POST /api/jobs/prioritize` or click "Run Prioritization Cycle" on the Recovery Planning board.
- Failure: Priority score formula adds `+ durationPenalty` instead of subtracting it. Lengthy 120-minute jobs (`JOB-104`) receive an inflated score boost over high-efficiency critical jobs (`JOB-101`), which is worsened by Bug 1's missed demand score.
- Expected: Processing duration overhead must reduce priority (`- durationPenalty`), allowing `JOB-101` to lead the queue when combined with the corrected matching output.

3. Total Capacity Check Over-Allocating Processing Stations
- Bug location: `src/main/java/com/debuglab/reloop/service/BatchAllocationService.java`, within `canStationAccommodate(ProcessingStation, RecoveryJob)` around lines 33–41.
- How to observe it: Attempt to allocate `JOB-104` (requires 35 units) to Station `STN-01` (max 100 units, 75 already allocated, 25 available) via `POST /api/allocations/job`.
- Failure: Available capacity is calculated as `station.getMaxCapacityUnits() - job.getRequiredCapacityUnits()` (100 - 35 = 65 >= 0) ignoring existing load. The allocation succeeds and allocated capacity rises to 110/100, exceeding station limits.
- Expected: Available capacity must be computed against current load (`station.getMaxCapacityUnits() - station.getAllocatedCapacityUnits()`) and reject jobs exceeding the remaining 25 units.

4. Defective Interval Containment in Technician Scheduling
- Bug location: `src/main/java/com/debuglab/reloop/service/TechnicianSchedulingService.java`, within `hasSchedulingConflict(Technician, LocalDateTime, LocalDateTime)` around lines 28–36.
- How to observe it: Schedule technician `TECH-01` (Marcus Vance, who has an existing 09:00–11:00 assignment) for a job starting at 10:00 and ending at 12:00 via `POST /api/scheduling/schedule`.
- Failure: Conflict detection checks `candidateStart.isAfter(existing.getStartTime()) && candidateEnd.isBefore(existing.getEndTime())`. Because 12:00 is not before 11:00, no conflict is detected and the technician is double-booked from 10:00 to 11:00.
- Expected: Overlapping intervals must be detected whenever `candidateStart.isBefore(existing.getEndTime()) && candidateEnd.isAfter(existing.getStartTime())`.

5. Premature Lifecycle State Bypass to Completed
- Bug location: `src/main/java/com/debuglab/reloop/service/RecoveryLifecycleService.java`, within the `ALLOWED_TRANSITIONS` map definition around lines 21–32.
- How to observe it: Call `POST /api/jobs/JOB-103/complete` or `POST /api/jobs/JOB-103/transition` with `targetState: "COMPLETED"` while `JOB-103` is in `READY_FOR_RECOVERY`.
- Failure: The state machine permits `READY_FOR_RECOVERY -> COMPLETED` directly, allowing unrecovered jobs to bypass scheduling and extraction workflows.
- Expected: Jobs in `READY_FOR_RECOVERY` can only transition to `SCHEDULED` or `REJECTED`. Only jobs that reach `RECOVERED` may transition to `COMPLETED`.

6. Incomplete Component Reservation Release on Cancellation
- Bug location: `src/main/java/com/debuglab/reloop/service/RecoveryReservationService.java`, within `cancelJob(String)` around lines 87–93.
- How to observe it: Call `POST /api/jobs/JOB-105/cancel` (which has component `CMP-107` reserved), then inspect `GET /api/components/CMP-107`.
- Failure: `cancelJob` sets `component.setReservedForJobId(null)` but fails to restore `component.setAvailabilityStatus(ComponentAvailability.AVAILABLE)`. The component remains stranded in `RESERVED` status and cannot be matched to future demands.
- Expected: Cancelling a job must set the component's status back to `AVAILABLE` and clear its reservation reference.
