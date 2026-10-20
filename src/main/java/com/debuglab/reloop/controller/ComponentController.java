package com.debuglab.reloop.controller;

import com.debuglab.reloop.model.CompatibilityResult;
import com.debuglab.reloop.model.Component;
import com.debuglab.reloop.model.ComponentRequirement;
import com.debuglab.reloop.repository.ComponentRepository;
import com.debuglab.reloop.repository.RequirementRepository;
import com.debuglab.reloop.service.ComponentMatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ComponentController {

    private final ComponentRepository componentRepository;
    private final RequirementRepository requirementRepository;
    private final ComponentMatchingService matchingService;

    public ComponentController(ComponentRepository componentRepository,
                               RequirementRepository requirementRepository,
                               ComponentMatchingService matchingService) {
        this.componentRepository = componentRepository;
        this.requirementRepository = requirementRepository;
        this.matchingService = matchingService;
    }

    @GetMapping("/components")
    public ResponseEntity<List<Component>> getAllComponents() {
        return ResponseEntity.ok(componentRepository.findAll());
    }

    @GetMapping("/components/{id}")
    public ResponseEntity<Component> getComponentById(@PathVariable String id) {
        return componentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/requirements")
    public ResponseEntity<List<ComponentRequirement>> getAllRequirements() {
        return ResponseEntity.ok(requirementRepository.findAll());
    }

    @GetMapping("/matching/evaluate")
    public ResponseEntity<CompatibilityResult> evaluateCandidate(
            @RequestParam String candidateId,
            @RequestParam String requirementId) {
        Optional<Component> compOpt = componentRepository.findById(candidateId);
        Optional<ComponentRequirement> reqOpt = requirementRepository.findById(requirementId);

        if (compOpt.isEmpty() || reqOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(CompatibilityResult.reject("Candidate or requirement not found", candidateId, requirementId));
        }

        return ResponseEntity.ok(matchingService.evaluateCompatibility(compOpt.get(), reqOpt.get()));
    }

    @PostMapping("/requirements/{id}/match")
    public ResponseEntity<?> matchRequirement(@PathVariable String id) {
        Optional<ComponentRequirement> reqOpt = requirementRepository.findById(id);
        if (reqOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ComponentRequirement req = reqOpt.get();

        List<Component> candidates = componentRepository.findAll();
        Optional<Component> bestMatch = matchingService.findBestMatch(req, candidates);

        Map<String, Object> response = new HashMap<>();
        response.put("requirement", req);
        if (bestMatch.isPresent()) {
            Component matched = bestMatch.get();
            CompatibilityResult result = matchingService.evaluateCompatibility(matched, req);
            response.put("matchedComponent", matched);
            response.put("compatibility", result);
        } else {
            response.put("matchedComponent", null);
            response.put("message", "No compatible available component found for requirement");
        }

        return ResponseEntity.ok(response);
    }
}
