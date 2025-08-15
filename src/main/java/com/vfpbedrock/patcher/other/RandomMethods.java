package com.vfpbedrock.patcher.other;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.FloatTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.connection.UserConnection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.raphimc.viabedrock.api.BedrockProtocolVersion;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.PlayerAuthInputPacket_InputData;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class RandomMethods {
    public static boolean hasVFPInit() {
        try {
            ViaFabricPlus.getImpl();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // TODO: Is this correct?
    public static VoxelShape boxComponentToVoxel(CompoundTag collision) {
        @Nullable ListTag<FloatTag> origin = (ListTag<FloatTag>) collision.getNumberListTag("origin");
        @Nullable ListTag<FloatTag> size = (ListTag<FloatTag>) collision.getNumberListTag("size");

        float minX = (origin.get(0).getValue() + 8) / 16F;
        float minY = origin.get(1).getValue() / 16F;
        float minZ = (origin.get(2).getValue() + 8F) / 16F;
        float maxX = minX + size.get(0).getValue() / 16F;
        float maxY = minY + (size.get(1).getValue() / 16F);
        float maxZ = minZ + (size.get(2).getValue() / 16F);

        return VoxelShapes.cuboidUnchecked(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static boolean canDoMixins(Object object, boolean version, boolean player) {
        version = !version || ViaFabricPlus.getImpl().getTargetVersion() == BedrockProtocolVersion.bedrockLatest;
        player = !player || object == MinecraftClient.getInstance().player;

        return version && player;
    }

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

    public static void stopGliding() {
        if (ViaFabricPlus.getImpl().getTargetVersion() != BedrockProtocolVersion.bedrockLatest) {
            return;
        }

        final UserConnection user = ViaFabricPlus.getImpl().getPlayNetworkUserConnection();
        if (user == null) {
            return;
        }

        user.get(EntityTracker.class).getClientPlayer().addAuthInputData(PlayerAuthInputPacket_InputData.StopGliding);
    }
}
