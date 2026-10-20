package com.debuglab.reloop.repository;

import com.debuglab.reloop.model.RecoveryJob;
import com.debuglab.reloop.model.WorkflowState;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class RecoveryJobRepository {
    private final Map<String, RecoveryJob> storage = new ConcurrentHashMap<>();

    public List<RecoveryJob> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<RecoveryJob> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<RecoveryJob> findByLifecycleStatus(WorkflowState status) {
        return storage.values().stream()
                .filter(job -> job.getLifecycleStatus() == status)
                .collect(Collectors.toList());
    }

    public List<RecoveryJob> findBySourceDeviceId(String deviceId) {
        return storage.values().stream()
                .filter(job -> deviceId.equals(job.getSourceDeviceId()))
                .collect(Collectors.toList());
    }

    public RecoveryJob save(RecoveryJob job) {
        storage.put(job.getId(), job);
        return job;
    }

    public void deleteById(String id) {
        storage.remove(id);
    }

    public void clear() {
        storage.clear();
    }
}
