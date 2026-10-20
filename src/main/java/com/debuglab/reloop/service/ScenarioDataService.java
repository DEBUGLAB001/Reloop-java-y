package com.debuglab.reloop.service;

import com.debuglab.reloop.model.AuditActivity;
import com.debuglab.reloop.model.Component;
import com.debuglab.reloop.model.ComponentAvailability;
import com.debuglab.reloop.model.ComponentRequirement;
import com.debuglab.reloop.model.ConditionGrade;
import com.debuglab.reloop.model.Device;
import com.debuglab.reloop.model.ProcessingStation;
import com.debuglab.reloop.model.RecoveryJob;
import com.debuglab.reloop.model.ScheduledAssignment;
import com.debuglab.reloop.model.Technician;
import com.debuglab.reloop.model.TechnicianSkill;
import com.debuglab.reloop.model.UrgencyLevel;
import com.debuglab.reloop.model.WorkflowState;
import com.debuglab.reloop.repository.ActivityRepository;
import com.debuglab.reloop.repository.ComponentRepository;
import com.debuglab.reloop.repository.DeviceRepository;
import com.debuglab.reloop.repository.ProcessingStationRepository;
import com.debuglab.reloop.repository.RecoveryJobRepository;
import com.debuglab.reloop.repository.RequirementRepository;
import com.debuglab.reloop.repository.TechnicianRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class ScenarioDataService {

    private final DeviceRepository deviceRepository;
    private final ComponentRepository componentRepository;
    private final RequirementRepository requirementRepository;
    private final RecoveryJobRepository jobRepository;
    private final ProcessingStationRepository stationRepository;
    private final TechnicianRepository technicianRepository;
    private final ActivityRepository activityRepository;

    public ScenarioDataService(DeviceRepository deviceRepository,
                               ComponentRepository componentRepository,
                               RequirementRepository requirementRepository,
                               RecoveryJobRepository jobRepository,
                               ProcessingStationRepository stationRepository,
                               TechnicianRepository technicianRepository,
                               ActivityRepository activityRepository) {
        this.deviceRepository = deviceRepository;
        this.componentRepository = componentRepository;
        this.requirementRepository = requirementRepository;
        this.jobRepository = jobRepository;
        this.stationRepository = stationRepository;
        this.technicianRepository = technicianRepository;
        this.activityRepository = activityRepository;
    }

    @PostConstruct
    public void init() {
        resetScenario();
    }

    public synchronized void resetScenario() {
        deviceRepository.clear();
        componentRepository.clear();
        requirementRepository.clear();
        jobRepository.clear();
        stationRepository.clear();
        technicianRepository.clear();
        activityRepository.clear();

        LocalDate today = LocalDate.now();

        // 1. Devices
        deviceRepository.save(new Device("DEV-101", "Lenovo ThinkPad X1 Carbon Gen 9", "Laptop", "Good",
                WorkflowState.READY_FOR_RECOVERY, LocalDateTime.of(today, LocalTime.of(8, 30)),
                List.of("CMP-101", "CMP-102", "CMP-103"), "Display and SSD tested in pristine condition. Ready for teardown."));

        deviceRepository.save(new Device("DEV-102", "Apple MacBook Pro 16 A2141", "Laptop", "Fair",
                WorkflowState.READY_FOR_RECOVERY, LocalDateTime.of(today, LocalTime.of(9, 15)),
                List.of("CMP-104", "CMP-105"), "Battery healthy, casing scuffed, display panel intact."));

        deviceRepository.save(new Device("DEV-103", "Dell Latitude 5420", "Laptop", "Fair",
                WorkflowState.READY_FOR_RECOVERY, LocalDateTime.of(today, LocalTime.of(10, 0)),
                List.of("CMP-106", "CMP-107"), "Functional motherboard, good RAM and standard SSD."));

        deviceRepository.save(new Device("DEV-104", "Microsoft Surface Pro 8", "Tablet", "Good",
                WorkflowState.READY_FOR_RECOVERY, LocalDateTime.of(today, LocalTime.of(10, 45)),
                List.of("CMP-108", "CMP-109"), "High-value touch assembly and fast internal NVMe drive."));

        deviceRepository.save(new Device("DEV-105", "HP EliteDesk 800 G6 Mini", "Mini PC", "Good",
                WorkflowState.RECEIVED, LocalDateTime.of(today, LocalTime.of(11, 20)),
                List.of(), "Received from corporate decommission. Awaiting diagnostic intake inspection."));

        deviceRepository.save(new Device("DEV-106", "Samsung Galaxy S22 Ultra", "Smartphone", "Fair",
                WorkflowState.INSPECTING, LocalDateTime.of(today, LocalTime.of(11, 40)),
                List.of(), "Undergoing cleanroom screen and battery health diagnostics."));

        // 2. Components
        componentRepository.save(new Component("CMP-101", "NVME_SSD", "Samsung", "ThinkPad",
                "M.2_PCIe_3.0", 1024, ConditionGrade.A, ComponentAvailability.AVAILABLE, "DEV-101", null));

        componentRepository.save(new Component("CMP-102", "DDR4_RAM", "Crucial", "Universal",
                "SO-DIMM_DDR4", 16, ConditionGrade.B, ComponentAvailability.AVAILABLE, "DEV-101", null));

        componentRepository.save(new Component("CMP-103", "DISPLAY_PANEL", "LG Display", "ThinkPad",
                "eDP_30PIN", 14, ConditionGrade.A, ComponentAvailability.AVAILABLE, "DEV-101", null));

        componentRepository.save(new Component("CMP-104", "BATTERY", "Simplo", "MacBook",
                "Proprietary_Apple", 96, ConditionGrade.B, ComponentAvailability.AVAILABLE, "DEV-102", null));

        componentRepository.save(new Component("CMP-105", "DISPLAY_PANEL", "LG Display", "MacBook",
                "eDP_40PIN", 16, ConditionGrade.B, ComponentAvailability.AVAILABLE, "DEV-102", null));

        componentRepository.save(new Component("CMP-106", "NVME_SSD", "Kioxia", "Dell_Latitude",
                "M.2_PCIe_3.0", 512, ConditionGrade.B, ComponentAvailability.AVAILABLE, "DEV-103", null));

        componentRepository.save(new Component("CMP-107", "DDR4_RAM", "SK Hynix", "Universal",
                "SO-DIMM_DDR4", 8, ConditionGrade.B, ComponentAvailability.RESERVED, "DEV-103", "JOB-105"));

        componentRepository.save(new Component("CMP-108", "NVME_SSD", "WD Black", "Surface",
                "M.2_PCIe_3.0", 512, ConditionGrade.B, ComponentAvailability.AVAILABLE, "DEV-104", null));

        componentRepository.save(new Component("CMP-109", "DISPLAY_PANEL", "Sharp", "Surface",
                "eDP_40PIN", 13, ConditionGrade.B, ComponentAvailability.AVAILABLE, "DEV-104", null));

        // 3. Requirements
        requirementRepository.save(new ComponentRequirement("REQ-201", "ThinkPad Corporate Fleet Refurbishment - High-Cap SSD",
                "NVME_SSD", 1024, "M.2_PCIe_3.0", ConditionGrade.B, "ThinkPad", UrgencyLevel.CRITICAL, null, "OPEN"));

        requirementRepository.save(new ComponentRequirement("REQ-202", "MacBook Enterprise Refresh - High-Capacity Battery Pack",
                "BATTERY", 80, "Proprietary_Apple", ConditionGrade.B, "MacBook", UrgencyLevel.HIGH, null, "OPEN"));

        requirementRepository.save(new ComponentRequirement("REQ-203", "Education Initiative Laptop Refresh - 512GB Storage",
                "NVME_SSD", 512, "M.2_PCIe_3.0", ConditionGrade.B, "Dell_Latitude", UrgencyLevel.MEDIUM, null, "OPEN"));

        // 4. Recovery Jobs
        jobRepository.save(new RecoveryJob("JOB-101", "ThinkPad X1 Precision Extraction", "DEV-101",
                "PRECISION_TEARDOWN", List.of("CMP-101", "CMP-102", "CMP-103"), 130.0, UrgencyLevel.CRITICAL,
                320.0, 30, 30, TechnicianSkill.DISASSEMBLY, WorkflowState.READY_FOR_RECOVERY,
                null, null, null, null, LocalDateTime.of(today, LocalTime.of(8, 45)), null, "Urgent job for ThinkPad fleet component harvesting."));

        jobRepository.save(new RecoveryJob("JOB-102", "MacBook Pro Battery & Screen Disassembly", "DEV-102",
                "PRECISION_TEARDOWN", List.of("CMP-104", "CMP-105"), 200.0, UrgencyLevel.HIGH,
                280.0, 90, 40, TechnicianSkill.DISASSEMBLY, WorkflowState.READY_FOR_RECOVERY,
                null, null, null, null, LocalDateTime.of(today, LocalTime.of(9, 30)), null, "Requires careful battery adhesive solvent removal."));

        jobRepository.save(new RecoveryJob("JOB-103", "Dell Latitude Harvesting & Cleaning", "DEV-103",
                "COMPONENT_HARVESTING", List.of("CMP-106"), 157.5, UrgencyLevel.MEDIUM,
                250.0, 45, 25, TechnicianSkill.COMPONENT_HARVESTING, WorkflowState.READY_FOR_RECOVERY,
                null, null, null, null, LocalDateTime.of(today, LocalTime.of(10, 15)), null, "Modular harvest of SSD and memory."));

        jobRepository.save(new RecoveryJob("JOB-104", "Surface Pro Display & Logic Board Teardown", "DEV-104",
                "PRECISION_TEARDOWN", List.of("CMP-108", "CMP-109"), 230.0, UrgencyLevel.CRITICAL,
                440.0, 120, 35, TechnicianSkill.DISASSEMBLY, WorkflowState.READY_FOR_RECOVERY,
                null, null, null, null, LocalDateTime.of(today, LocalTime.of(10, 50)), null, "Complex heat plate teardown for glued Surface digitizer."));

        jobRepository.save(new RecoveryJob("JOB-105", "Dell Memory Module Extraction & Test", "DEV-103",
                "COMPONENT_HARVESTING", List.of("CMP-107"), 100.0, UrgencyLevel.LOW,
                60.0, 30, 15, TechnicianSkill.COMPONENT_HARVESTING, WorkflowState.SCHEDULED,
                "STN-02", "TECH-03", LocalDateTime.of(today, LocalTime.of(14, 0)), LocalDateTime.of(today, LocalTime.of(14, 30)),
                LocalDateTime.of(today, LocalTime.of(11, 0)), null, "Reserved allocation test job."));

        // 5. Processing Stations
        stationRepository.save(new ProcessingStation("STN-01", "Precision Teardown Bay Alpha",
                Set.of("PRECISION_TEARDOWN", "DISASSEMBLY"), 100, 75, "ONLINE"));

        stationRepository.save(new ProcessingStation("STN-02", "Component Harvesting Bay Beta",
                Set.of("PRECISION_TEARDOWN", "COMPONENT_HARVESTING"), 120, 30, "ONLINE"));

        stationRepository.save(new ProcessingStation("STN-03", "Diagnostic & Micro-Soldering Lab",
                Set.of("MICROSOLDERING", "FIRMWARE_FLASH"), 80, 20, "ONLINE"));

        // 6. Technicians
        technicianRepository.save(new Technician("TECH-01", "Marcus Vance",
                Set.of(TechnicianSkill.DISASSEMBLY, TechnicianSkill.COMPONENT_HARVESTING),
                LocalTime.of(8, 0), LocalTime.of(17, 0),
                List.of(new ScheduledAssignment("PREV-JOB-99",
                        LocalDateTime.of(today, LocalTime.of(9, 0)),
                        LocalDateTime.of(today, LocalTime.of(11, 0)),
                        "DISASSEMBLY"))));

        technicianRepository.save(new Technician("TECH-02", "Elena Rostova",
                Set.of(TechnicianSkill.MICROSOLDERING, TechnicianSkill.DISASSEMBLY, TechnicianSkill.FIRMWARE_FLASH),
                LocalTime.of(8, 0), LocalTime.of(17, 0),
                List.of()));

        technicianRepository.save(new Technician("TECH-03", "Devon Reed",
                Set.of(TechnicianSkill.COMPONENT_HARVESTING, TechnicianSkill.CLEANROOM_ASSEMBLY),
                LocalTime.of(9, 0), LocalTime.of(18, 0),
                List.of(new ScheduledAssignment("JOB-105",
                        LocalDateTime.of(today, LocalTime.of(14, 0)),
                        LocalDateTime.of(today, LocalTime.of(14, 30)),
                        "COMPONENT_HARVESTING"))));

        // 7. Initial Activity Log
        activityRepository.save(new AuditActivity(UUID.randomUUID().toString(),
                LocalDateTime.of(today, LocalTime.of(8, 0)),
                "SYSTEM_INITIALIZED", "System", "RELOOP-SYS",
                "ReLoop operations platform initialized with standard circular refurbishment scenario."));
    }
}
