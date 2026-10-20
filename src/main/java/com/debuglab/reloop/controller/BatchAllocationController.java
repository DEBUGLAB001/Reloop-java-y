package com.debuglab.reloop.controller;

import com.debuglab.reloop.model.AllocationResult;
import com.debuglab.reloop.model.ProcessingStation;
import com.debuglab.reloop.repository.ProcessingStationRepository;
import com.debuglab.reloop.service.BatchAllocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BatchAllocationController {

    private final ProcessingStationRepository stationRepository;
    private final BatchAllocationService batchAllocationService;

    public BatchAllocationController(ProcessingStationRepository stationRepository,
                                   BatchAllocationService batchAllocationService) {
        this.stationRepository = stationRepository;
        this.batchAllocationService = batchAllocationService;
    }

    @GetMapping("/stations")
    public ResponseEntity<List<ProcessingStation>> getAllStations() {
        return ResponseEntity.ok(stationRepository.findAll());
    }

    public static class JobAllocationDto {
        private String jobId;
        private String stationId;

        public String getJobId() { return jobId; }
        public void setJobId(String jobId) { this.jobId = jobId; }
        public String getStationId() { return stationId; }
        public void setStationId(String stationId) { this.stationId = stationId; }
    }

    @PostMapping("/allocations/job")
    public ResponseEntity<AllocationResult> allocateJob(@RequestBody JobAllocationDto dto) {
        AllocationResult result = batchAllocationService.allocateJobToStation(dto.getJobId(), dto.getStationId());
        if (!result.isSuccessful()) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    public static class BatchAllocationDto {
        private List<String> jobIds;

        public List<String> getJobIds() { return jobIds; }
        public void setJobIds(List<String> jobIds) { this.jobIds = jobIds; }
    }

    @PostMapping("/allocations/batch")
    public ResponseEntity<List<AllocationResult>> allocateBatch(@RequestBody BatchAllocationDto dto) {
        if (dto.getJobIds() == null || dto.getJobIds().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<AllocationResult> results = batchAllocationService.allocateBatch(dto.getJobIds());
        return ResponseEntity.ok(results);
    }
}
