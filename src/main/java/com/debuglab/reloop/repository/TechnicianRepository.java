package com.debuglab.reloop.repository;

import com.debuglab.reloop.model.Technician;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TechnicianRepository {
    private final Map<String, Technician> storage = new ConcurrentHashMap<>();

    public List<Technician> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<Technician> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Technician save(Technician technician) {
        storage.put(technician.getId(), technician);
        return technician;
    }

    public void deleteById(String id) {
        storage.remove(id);
    }

    public void clear() {
        storage.clear();
    }
}
