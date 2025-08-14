package com.vfpbedrock.patcher.mixins.local.entity;

import com.vfpbedrock.patcher.other.RandomMethods;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.connection.UserConnection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.raphimc.viabedrock.api.BedrockProtocolVersion;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.PlayerAuthInputPacket_InputData;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixins {
    @Inject(method = "jump", at = @At("HEAD"))
    public void addStartJumpingBedrock(CallbackInfo ci) {
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

        user.get(EntityTracker.class).getClientPlayer().addAuthInputData(PlayerAuthInputPacket_InputData.StartJumping);
    }

    @Inject(method = "stopGliding", at = @At("HEAD"))
    public void addStopGlidingBedrock1(CallbackInfo ci) {
        if (((Object)this) != MinecraftClient.getInstance().player) {
            return;
        }

        RandomMethods.stopGliding();
    }

    @Inject(method = "tickGliding", at = @At(value = "HEAD"))
    public void addStopGlidingBedrock2(CallbackInfo ci) {
        if (((Object)this) != MinecraftClient.getInstance().player) {
            return;
        }

        if (!this.canGlide()) {
            stopGliding();
            RandomMethods.stopGliding();
        }
    }

    @Shadow
    protected abstract boolean canGlide();
    @Shadow
    public abstract void stopGliding();
}
