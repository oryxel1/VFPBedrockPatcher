package com.vfpbedrock.patcher.tracker;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import net.raphimc.viabedrock.api.model.entity.Entity;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class VehicleTracker extends StoredObject {
    private long vehicleRuntimeId = -1;
    private final Map<Entity, Set<Long>> entityToRiders = new ConcurrentHashMap<>();
    public VehicleTracker(UserConnection user) {
        super(user);
    }

    public long vehicleRuntimeId() {
        return vehicleRuntimeId;
    }

    public void vehicleRuntimeId(long vehicleRuntimeId) {
        this.vehicleRuntimeId = vehicleRuntimeId;
    }

    public Map<Entity, Set<Long>> entityToRiders() {
        return entityToRiders;
    }
}
