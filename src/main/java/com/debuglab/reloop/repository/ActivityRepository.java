package com.debuglab.reloop.repository;

import com.debuglab.reloop.model.AuditActivity;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ActivityRepository {
    private final Map<String, AuditActivity> storage = new ConcurrentHashMap<>();

    public List<AuditActivity> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(AuditActivity::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public AuditActivity save(AuditActivity activity) {
        storage.put(activity.getId(), activity);
        return activity;
    }

    public void clear() {
        storage.clear();
    }
}
