package com.vfpbedrock.patcher.mixins.local.network;

import com.vfpbedrock.patcher.tracker.TeleportTracker;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.connection.UserConnection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.raphimc.viabedrock.api.BedrockProtocolVersion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixins {
    @Shadow
    public abstract ClientConnection getConnection();

    @Inject(method = "onPlayerPositionLook", at = @At("HEAD"), cancellable = true)
    public void doBedrockTeleport(PlayerPositionLookS2CPacket packet, CallbackInfo ci) {
        final UserConnection user = ViaFabricPlus.getImpl().getUserConnection(this.getConnection());
        if (user == null || ViaFabricPlus.getImpl().getTargetVersion() != BedrockProtocolVersion.bedrockLatest) {
            return;
        }

        if (packet.teleportId() < 0) { // Fake teleport, ignore this.
            return;
        }

        ci.cancel();

        final MinecraftClient client = MinecraftClient.getInstance();

        NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, client);
        PlayerEntity playerEntity = client.player;
        getConnection().send(new TeleportConfirmC2SPacket(packet.teleportId()));
        if (playerEntity.hasVehicle()) {
            getConnection().send(new PlayerMoveC2SPacket.Full(playerEntity.getX(), playerEntity.getY(), playerEntity.getZ(), playerEntity.getYaw(), playerEntity.getPitch(), false, false));
            return;
        }

        // On bedrock player set position after ticking movement so we cache it and do the same.
        PlayerPosition playerPosition = PlayerPosition.fromEntity(playerEntity);
        PlayerPosition playerPosition2 = PlayerPosition.apply(playerPosition, packet.change(), packet.relatives());
        getConnection().send(new PlayerMoveC2SPacket.Full(playerPosition2.position().getX(),
                playerPosition2.position().getY(), playerPosition2.position().getZ(),
                playerPosition2.yaw(), playerPosition2.pitch(), false, false));

        user.get(TeleportTracker.class).cachedTeleportPosition(packet);
    }
}
