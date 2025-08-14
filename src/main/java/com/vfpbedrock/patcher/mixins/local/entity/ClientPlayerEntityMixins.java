package com.vfpbedrock.patcher.mixins.local.entity;

import com.vfpbedrock.patcher.other.RandomMethods;
import com.vfpbedrock.patcher.tracker.TeleportTracker;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.connection.UserConnection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.Hand;
import net.raphimc.viabedrock.api.BedrockProtocolVersion;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.PlayerAuthInputPacket_InputData;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixins {
    @Inject(method = "setCurrentHand", at = @At("HEAD"))
    public void startUsingItem(Hand hand, CallbackInfo ci) {
        if (((Object)this) != MinecraftClient.getInstance().player) {
            return;
        }

        if (ViaFabricPlus.getImpl().getTargetVersion() != BedrockProtocolVersion.bedrockLatest) {
            return;
        }

        final UserConnection user = ViaFabricPlus.getImpl().getPlayNetworkUserConnection();
        if (user == null) {
            return;
        }

        user.get(EntityTracker.class).getClientPlayer().addAuthInputData(PlayerAuthInputPacket_InputData.StartUsingItem);
    }

    @Inject(method = "sendMovementPackets", at = @At("HEAD"))
    public void doBedrockTeleport(CallbackInfo ci) {
        if (((Object)this) != MinecraftClient.getInstance().player) {
            return;
        }

        if (ViaFabricPlus.getImpl().getTargetVersion() != BedrockProtocolVersion.bedrockLatest) {
            return;
        }

        final UserConnection user = ViaFabricPlus.getImpl().getPlayNetworkUserConnection();
        if (user == null) {
            return;
        }

        final PlayerPositionLookS2CPacket packet = user.get(TeleportTracker.class).cachedTeleportPosition();
        if (packet == null) {
            return;
        }

        // Handle the cached bedrock teleport :D
        RandomMethods.setPosition(packet.change(), packet.relatives(), (PlayerEntity) (Object) this, false);
        user.get(TeleportTracker.class).cachedTeleportPosition(null);
    }
}
