package com.vfpbedrock.patcher.tracker;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class TeleportTracker extends StoredObject {
    private PlayerPositionLookS2CPacket cachedTeleport = null;

    public TeleportTracker(UserConnection user) {
        super(user);
    }

    public PlayerPositionLookS2CPacket cachedTeleportPosition() {
        return cachedTeleport;
    }

    public void cachedTeleportPosition(final PlayerPositionLookS2CPacket cachedTeleportPosition) {
        this.cachedTeleport = cachedTeleportPosition;
    }
}
