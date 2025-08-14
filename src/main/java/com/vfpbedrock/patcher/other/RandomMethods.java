package com.vfpbedrock.patcher.other;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class RandomMethods {
    public static boolean setPosition(PlayerPosition pos, Set<PositionFlag> flags, Entity entity, boolean bl) {
        PlayerPosition playerPosition = PlayerPosition.fromEntity(entity);
        PlayerPosition playerPosition2 = PlayerPosition.apply(playerPosition, pos, flags);
        boolean bl2 = playerPosition.position().squaredDistanceTo(playerPosition2.position()) > (double)4096.0F;
        if (bl && !bl2) {
            entity.updateTrackedPositionAndAngles(playerPosition2.position(), playerPosition2.yaw(), playerPosition2.pitch());
            entity.setVelocity(playerPosition2.deltaMovement());
            return true;
        } else {
            entity.setPosition(playerPosition2.position());
            entity.setVelocity(playerPosition2.deltaMovement());
            entity.setYaw(playerPosition2.yaw());
            entity.setPitch(playerPosition2.pitch());
            PlayerPosition playerPosition3 = new PlayerPosition(entity.getLastRenderPos(), Vec3d.ZERO, entity.lastYaw, entity.lastPitch);
            PlayerPosition playerPosition4 = PlayerPosition.apply(playerPosition3, pos, flags);
            entity.setLastPositionAndAngles(playerPosition4.position(), playerPosition4.yaw(), playerPosition4.pitch());
            return false;
        }
    }
}
