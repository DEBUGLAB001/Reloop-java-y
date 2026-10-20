package com.debuglab.reloop.service;

import com.debuglab.reloop.model.DemandMatchSummary;
import com.debuglab.reloop.model.RecoveryJob;
import com.debuglab.reloop.repository.RecoveryJobRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecoveryPriorityService {

    private final RecoveryJobRepository recoveryJobRepository;
    private final ComponentMatchingService componentMatchingService;

    public RecoveryPriorityService(RecoveryJobRepository recoveryJobRepository,
                                  ComponentMatchingService componentMatchingService) {
        this.recoveryJobRepository = recoveryJobRepository;
        this.componentMatchingService = componentMatchingService;
    }

    public double calculateJobPriorityScore(RecoveryJob job) {
        DemandMatchSummary matchSummary = componentMatchingService.evaluateDemandCoverage(job.getRecoverableComponentIds());
        double demandScore = matchSummary.getDemandScore();
        double valueScore = job.getEstimatedRecoveryValue() * 0.25;
        double urgencyScore = job.getUrgency().getWeight();
        double durationPenalty = (job.getProcessingDurationMinutes() / 15.0) * 5.0;

        return demandScore + valueScore + urgencyScore + durationPenalty;
    }

    public List<RecoveryJob> prioritizeEligibleJobs() {
        List<RecoveryJob> jobs = recoveryJobRepository.findAll();
        Map<String, Double> scoreMap = new HashMap<>();

        for (RecoveryJob job : jobs) {
            double score = calculateJobPriorityScore(job);
            job.setPriorityScore(score);
            scoreMap.put(job.getId(), score);
            recoveryJobRepository.save(job);
        }

        return jobs.stream()
                .sorted(Comparator.comparingDouble((RecoveryJob j) -> scoreMap.getOrDefault(j.getId(), 0.0)).reversed()
                        .thenComparing(RecoveryJob::getId))
                .toList();
    }
}
