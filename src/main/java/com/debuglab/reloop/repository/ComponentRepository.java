package com.debuglab.reloop.repository;

import com.debuglab.reloop.model.Component;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ComponentRepository {
    private final Map<String, Component> storage = new ConcurrentHashMap<>();

    public List<Component> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<Component> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Component> findBySourceDeviceId(String deviceId) {
        return storage.values().stream()
                .filter(c -> deviceId.equals(c.getSourceDeviceId()))
                .collect(Collectors.toList());
    }

    public List<Component> findByReservedForJobId(String jobId) {
        return storage.values().stream()
                .filter(c -> jobId.equals(c.getReservedForJobId()))
                .collect(Collectors.toList());
    }

    public Component save(Component component) {
        storage.put(component.getId(), component);
        return component;
    }

    public void deleteById(String id) {
        storage.remove(id);
    }

    public void clear() {
        storage.clear();
    }
}
