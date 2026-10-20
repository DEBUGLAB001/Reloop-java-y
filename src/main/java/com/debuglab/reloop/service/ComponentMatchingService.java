package com.debuglab.reloop.service;

import com.debuglab.reloop.model.CompatibilityResult;
import com.debuglab.reloop.model.Component;
import com.debuglab.reloop.model.ComponentRequirement;
import com.debuglab.reloop.model.ConditionGrade;
import com.debuglab.reloop.model.DemandMatchSummary;
import com.debuglab.reloop.repository.ComponentRepository;
import com.debuglab.reloop.repository.RequirementRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ComponentMatchingService {

    private final ComponentRepository componentRepository;
    private final RequirementRepository requirementRepository;

    public ComponentMatchingService(ComponentRepository componentRepository,
                                    RequirementRepository requirementRepository) {
        this.componentRepository = componentRepository;
        this.requirementRepository = requirementRepository;
    }

    public CompatibilityResult evaluateCompatibility(Component candidate, ComponentRequirement requirement) {
        if (candidate == null) {
            return CompatibilityResult.reject("Candidate component is null", null, requirement.getId());
        }
        if (requirement == null) {
            return CompatibilityResult.reject("Requirement is null", candidate.getId(), null);
        }

        if (!candidate.isAvailable()) {
            return CompatibilityResult.reject("Component is currently unavailable or reserved", candidate.getId(), requirement.getId());
        }

        if (!candidate.getComponentType().equalsIgnoreCase(requirement.getRequiredType())) {
            return CompatibilityResult.reject(
                    String.format("Incompatible component type: expected %s, found %s",
                            requirement.getRequiredType(), candidate.getComponentType()),
                    candidate.getId(), requirement.getId());
        }

        if (candidate.getCapacity() < requirement.getMinimumCapacity()) {
            return CompatibilityResult.reject(
                    String.format("Insufficient capacity: %d < required %d",
                            candidate.getCapacity(), requirement.getMinimumCapacity()),
                    candidate.getId(), requirement.getId());
        }

        if (requirement.getInterfaceType() != null && !requirement.getInterfaceType().isBlank()) {
            if (!requirement.getInterfaceType().equalsIgnoreCase(candidate.getInterfaceType())) {
                return CompatibilityResult.reject(
                        String.format("Interface mismatch: expected %s, found %s",
                                requirement.getInterfaceType(), candidate.getInterfaceType()),
                        candidate.getId(), requirement.getId());
            }
        }

        if (requirement.getTargetModelFamily() != null && !requirement.getTargetModelFamily().isBlank()
                && !requirement.getTargetModelFamily().equalsIgnoreCase("Universal")) {
            if (candidate.getCompatibilityFamily() != null && !candidate.getCompatibilityFamily().isBlank()
                    && !candidate.getCompatibilityFamily().equalsIgnoreCase("Universal")
                    && !candidate.getCompatibilityFamily().equalsIgnoreCase(requirement.getTargetModelFamily())) {
                return CompatibilityResult.reject(
                        String.format("Model family mismatch: expected %s, found %s",
                                requirement.getTargetModelFamily(), candidate.getCompatibilityFamily()),
                        candidate.getId(), requirement.getId());
            }
        }

        if (!isGradeAcceptable(candidate.getConditionGrade(), requirement.getMinimumConditionGrade())) {
            return CompatibilityResult.reject(
                    String.format("Condition grade %s does not meet minimum required grade %s",
                            candidate.getConditionGrade(), requirement.getMinimumConditionGrade()),
                    candidate.getId(), requirement.getId());
        }

        int score = calculateCompatibilityScore(candidate, requirement);
        return CompatibilityResult.match(score, "Fully compatible with requirement specifications", candidate.getId(), requirement.getId());
    }

    public boolean isGradeAcceptable(ConditionGrade candidateGrade, ConditionGrade minimumGrade) {
        if (candidateGrade == null || minimumGrade == null) {
            return false;
        }
        return candidateGrade.ordinal() >= minimumGrade.ordinal();
    }

    public int calculateCompatibilityScore(Component candidate, ComponentRequirement requirement) {
        int baseScore = 50;
        int capacityBonus = Math.min(20, (candidate.getCapacity() - requirement.getMinimumCapacity()) / 10);
        int gradeBonus = (3 - candidate.getConditionGrade().ordinal()) * 10;
        return baseScore + Math.max(0, capacityBonus) + gradeBonus;
    }

    public Optional<Component> findBestMatch(ComponentRequirement requirement, List<Component> candidates) {
        return candidates.stream()
                .filter(candidate -> evaluateCompatibility(candidate, requirement).isCompatible())
                .max(Comparator.comparingInt(c -> calculateCompatibilityScore(c, requirement)));
    }

    public DemandMatchSummary evaluateDemandCoverage(List<String> componentIds) {
        if (componentIds == null || componentIds.isEmpty()) {
            return DemandMatchSummary.EMPTY;
        }

        List<ComponentRequirement> openRequirements = requirementRepository.findAll().stream()
                .filter(r -> "OPEN".equalsIgnoreCase(r.getStatus()))
                .sorted(Comparator.comparing((ComponentRequirement r) -> r.getUrgency().getWeight()).reversed())
                .toList();

        List<Component> components = componentIds.stream()
                .map(componentRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        double totalDemandScore = 0.0;
        List<String> matchedReqIds = new ArrayList<>();
        List<String> matchedCompIds = new ArrayList<>();
        Set<String> usedCompIds = new HashSet<>();

        for (ComponentRequirement req : openRequirements) {
            for (Component comp : components) {
                if (!usedCompIds.contains(comp.getId())) {
                    CompatibilityResult result = evaluateCompatibility(comp, req);
                    if (result.isCompatible()) {
                        usedCompIds.add(comp.getId());
                        matchedReqIds.add(req.getId());
                        matchedCompIds.add(comp.getId());
                        totalDemandScore += 50.0 + req.getUrgency().getWeight();
                        break;
                    }
                }
            }
        }

        return new DemandMatchSummary(totalDemandScore, matchedReqIds.size(), matchedReqIds, matchedCompIds);
    }
}
