package com.vfpbedrock.patcher.mixins.local.entity;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.connection.UserConnection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.math.Vec3d;
import net.raphimc.viabedrock.api.BedrockProtocolVersion;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.PlayerAuthInputPacket_InputData;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixins {
    @Shadow
    public boolean verticalCollision;

    @Inject(method = "shouldControlVehicles", at = @At("HEAD"), cancellable = true)
    public void changeControlCondition(CallbackInfoReturnable<Boolean> cir) {
        if (ViaFabricPlus.getImpl().getTargetVersion() != BedrockProtocolVersion.bedrockLatest) {
            return;
        }

        final UserConnection user = ViaFabricPlus.getImpl().getPlayNetworkUserConnection();
        if (user == null) {
            return;
        }

        Entity entity = (Entity) (Object) this;
        cir.setReturnValue(entity instanceof AbstractBoatEntity || entity instanceof AbstractHorseEntity);
    }

    @Inject(method = "setSwimming", at = @At("HEAD"))
    public void updateSwimmingBedrock(boolean swimming, CallbackInfo ci) {
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

        if (swimming != isSwimming()) {
            user.get(EntityTracker.class).getClientPlayer().addAuthInputData(swimming ? PlayerAuthInputPacket_InputData.StartSwimming : PlayerAuthInputPacket_InputData.StopSwimming);
        }
    }

    @Inject(method = "move", at = @At("TAIL"))
    public void updateVerticalCollisionBedrock(MovementType type, Vec3d movement, CallbackInfo ci) {
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

        if (this.verticalCollision) {
            user.get(EntityTracker.class).getClientPlayer().addAuthInputData(PlayerAuthInputPacket_InputData.VerticalCollision);
        }
    }

    @Shadow
    public abstract boolean isSwimming();
}
