package com.debuglab.reloop.service;

import com.debuglab.reloop.model.AllocationResult;
import com.debuglab.reloop.model.ProcessingStation;
import com.debuglab.reloop.model.RecoveryJob;
import com.debuglab.reloop.repository.ProcessingStationRepository;
import com.debuglab.reloop.repository.RecoveryJobRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class BatchAllocationService {

    private final ProcessingStationRepository stationRepository;
    private final RecoveryJobRepository jobRepository;

    public BatchAllocationService(ProcessingStationRepository stationRepository,
                                  RecoveryJobRepository jobRepository) {
        this.stationRepository = stationRepository;
        this.jobRepository = jobRepository;
    }

    public boolean canStationAccommodate(ProcessingStation station, RecoveryJob job) {
        if (station == null || job == null) {
            return false;
        }
        if (!station.isOperational()) {
            return false;
        }
        if (!station.supportsOperation(job.getRequiredOperation())) {
            return false;
        }

        int availableCapacity = station.getMaxCapacityUnits() - job.getRequiredCapacityUnits();
        return availableCapacity >= 0;
    }

    public Optional<ProcessingStation> findEligibleStation(RecoveryJob job) {
        List<ProcessingStation> stations = stationRepository.findAll();
        return stations.stream()
                .filter(s -> canStationAccommodate(s, job))
                .min(Comparator.comparingInt(ProcessingStation::getAllocatedCapacityUnits));
    }

    public AllocationResult allocateJobToStation(String jobId, String stationId) {
        Optional<RecoveryJob> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            return AllocationResult.failure(jobId, "Recovery job not found: " + jobId);
        }
        RecoveryJob job = jobOpt.get();

        Optional<ProcessingStation> stationOpt = stationRepository.findById(stationId);
        if (stationOpt.isEmpty()) {
            return AllocationResult.failure(jobId, "Processing station not found: " + stationId);
        }
        ProcessingStation station = stationOpt.get();

        if (!canStationAccommodate(station, job)) {
            return AllocationResult.failure(jobId,
                    String.format("Station %s cannot accommodate job %s: insufficient capacity or unsupported operation",
                            station.getName(), job.getTitle()));
        }

        station.allocateCapacity(job.getRequiredCapacityUnits());
        stationRepository.save(station);

        job.setAssignedStationId(station.getId());
        jobRepository.save(job);

        return AllocationResult.success(station.getId(), job.getId(), job.getRequiredCapacityUnits(),
                String.format("Allocated %d capacity units on %s", job.getRequiredCapacityUnits(), station.getName()));
    }

    public List<AllocationResult> allocateBatch(List<String> jobIds) {
        List<AllocationResult> results = new ArrayList<>();
        for (String jobId : jobIds) {
            Optional<RecoveryJob> jobOpt = jobRepository.findById(jobId);
            if (jobOpt.isPresent()) {
                RecoveryJob job = jobOpt.get();
                Optional<ProcessingStation> station = findEligibleStation(job);
                if (station.isPresent()) {
                    results.add(allocateJobToStation(job.getId(), station.get().getId()));
                } else {
                    results.add(AllocationResult.failure(job.getId(), "No available station with sufficient capacity"));
                }
            }
        }
        return results;
    }
}
