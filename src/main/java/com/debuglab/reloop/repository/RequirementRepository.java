package com.debuglab.reloop.repository;

import com.debuglab.reloop.model.ComponentRequirement;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RequirementRepository {
    private final Map<String, ComponentRequirement> storage = new ConcurrentHashMap<>();

    public List<ComponentRequirement> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<ComponentRequirement> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    public ComponentRequirement save(ComponentRequirement requirement) {
        storage.put(requirement.getId(), requirement);
        return requirement;
    }

    public void deleteById(String id) {
        storage.remove(id);
    }

    public void clear() {
        storage.clear();
    }
}
