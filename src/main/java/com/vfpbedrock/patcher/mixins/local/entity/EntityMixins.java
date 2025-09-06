package com.vfpbedrock.patcher.mixins.local.entity;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.connection.UserConnection;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.util.math.Vec3d;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.PlayerAuthInputPacket_InputData;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.vfpbedrock.patcher.other.RandomMethods.canDoMixins;

@Mixin(Entity.class)
public abstract class EntityMixins {
    @Shadow
    public boolean verticalCollision;

    @Redirect(method = "setPosition(DDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setPos(DDD)V"))
    public void setPos(Entity instance, double x, double y, double z) {
        if (!canDoMixins(this, true, true)) {
            instance.setPos(x, y, z);
            return;
        }

        float floatX = (float) x, floatY = (float) y, floatZ = (float) z;

        // Simulate floating point errors like on Bedrock.
        instance.setPos(floatX, floatY, floatZ);
    }

    @Inject(method = "shouldControlVehicles", at = @At("HEAD"), cancellable = true)
    public void changeControlCondition(CallbackInfoReturnable<Boolean> cir) {
        if (!canDoMixins(this, true, false)) {
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
        if (!canDoMixins(this, true, true)) {
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
        if (!canDoMixins(this, true, true)) {
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
    protected Vec3d movementMultiplier;

    @Inject(method = "slowMovement", at = @At("HEAD"), cancellable = true)
    public void bedrockSlowMovement(BlockState state, Vec3d multiplier, CallbackInfo ci) {
        if (!canDoMixins(this, true, true)) {
            return;
        }
        ci.cancel();
        onLanding();

        if (this.movementMultiplier != null && this.movementMultiplier.lengthSquared() > 1.0E-7) {
            this.movementMultiplier = new Vec3d(
                    Math.min(movementMultiplier.x, multiplier.x),
                    Math.min(movementMultiplier.y, multiplier.y),
                    Math.min(movementMultiplier.z, multiplier.z)
            );
        } else {
            this.movementMultiplier = multiplier;
        }
    }

    @Shadow
    public abstract void onLanding();

    @Shadow
    public abstract boolean isSwimming();
}
