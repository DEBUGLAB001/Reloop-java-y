package com.debuglab.reloop.controller;

import com.debuglab.reloop.model.SchedulingResult;
import com.debuglab.reloop.model.Technician;
import com.debuglab.reloop.repository.TechnicianRepository;
import com.debuglab.reloop.service.TechnicianSchedulingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class TechnicianSchedulingController {

    private final TechnicianRepository technicianRepository;
    private final TechnicianSchedulingService schedulingService;

    public TechnicianSchedulingController(TechnicianRepository technicianRepository,
                                         TechnicianSchedulingService schedulingService) {
        this.technicianRepository = technicianRepository;
        this.schedulingService = schedulingService;
    }

    @GetMapping("/technicians")
    public ResponseEntity<List<Technician>> getAllTechnicians() {
        return ResponseEntity.ok(technicianRepository.findAll());
    }

    public static class ScheduleDto {
        private String jobId;
        private String technicianId;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private LocalDateTime startTime;

        public String getJobId() { return jobId; }
        public void setJobId(String jobId) { this.jobId = jobId; }
        public String getTechnicianId() { return technicianId; }
        public void setTechnicianId(String technicianId) { this.technicianId = technicianId; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    }

    @PostMapping("/scheduling/schedule")
    public ResponseEntity<SchedulingResult> scheduleJob(@RequestBody ScheduleDto dto) {
        if (dto.getJobId() == null || dto.getTechnicianId() == null || dto.getStartTime() == null) {
            return ResponseEntity.badRequest().body(SchedulingResult.failure(dto.getJobId(), "Missing required scheduling fields"));
        }
        SchedulingResult result = schedulingService.scheduleJob(dto.getJobId(), dto.getTechnicianId(), dto.getStartTime());
        if (!result.isSuccessful()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }
}
