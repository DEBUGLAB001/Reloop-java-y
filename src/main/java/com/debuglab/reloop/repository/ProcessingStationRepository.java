package com.debuglab.reloop.repository;

import com.debuglab.reloop.model.ProcessingStation;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ProcessingStationRepository {
    private final Map<String, ProcessingStation> storage = new ConcurrentHashMap<>();

    public List<ProcessingStation> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<ProcessingStation> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    public ProcessingStation save(ProcessingStation station) {
        storage.put(station.getId(), station);
        return station;
    }

    public void deleteById(String id) {
        storage.remove(id);
    }

    public void clear() {
        storage.clear();
    }
}
