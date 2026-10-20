package com.debuglab.reloop.repository;

import com.debuglab.reloop.model.Device;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DeviceRepository {
    private final Map<String, Device> storage = new ConcurrentHashMap<>();

    public List<Device> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<Device> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Device save(Device device) {
        storage.put(device.getId(), device);
        return device;
    }

    public void deleteById(String id) {
        storage.remove(id);
    }

    public void clear() {
        storage.clear();
    }
}
