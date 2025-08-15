package com.vfpbedrock.patcher.mixins.local.input;

import com.vfpbedrock.patcher.protocol.enums.ClientInputLocksFlag;
import com.vfpbedrock.patcher.tracker.InputTracker;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.connection.UserConnection;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.vfpbedrock.patcher.other.RandomMethods.canDoMixins;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixins extends Input {
    @Shadow
    @Final
    private GameOptions settings;

    @Shadow
    private static float getMovementMultiplier(boolean positive, boolean negative) {
        return 0F;
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void lockInputWhenNeeded(CallbackInfo ci) {
        if (!canDoMixins(this, true, false)) {
            return;
        }

        final UserConnection user = ViaFabricPlus.getImpl().getPlayNetworkUserConnection();
        if (user == null) {
            return;
        }

        ci.cancel();
        final InputTracker tracker = user.get(InputTracker.class);
        final boolean movementLocked = tracker.has(ClientInputLocksFlag.MOVEMENT) || tracker.has(ClientInputLocksFlag.LATERAL_MOVEMENT);

        boolean forward = !tracker.has(ClientInputLocksFlag.MOVE_FORWARD) && !movementLocked && this.settings.forwardKey.isPressed();
        boolean backward = !tracker.has(ClientInputLocksFlag.MOVE_BACKWARD) && !movementLocked && this.settings.backKey.isPressed();
        boolean left = !tracker.has(ClientInputLocksFlag.MOVE_LEFT) && !movementLocked && this.settings.leftKey.isPressed();
        boolean right = !tracker.has(ClientInputLocksFlag.MOVE_RIGHT) && !movementLocked && this.settings.rightKey.isPressed();

        boolean jump = this.settings.jumpKey.isPressed() && !tracker.has(ClientInputLocksFlag.MOVEMENT) && !tracker.has(ClientInputLocksFlag.JUMP);
        boolean sneak = this.settings.sneakKey.isPressed() && !tracker.has(ClientInputLocksFlag.SNEAK);

        this.playerInput = new PlayerInput(forward, backward, left, right, jump, sneak, this.settings.sprintKey.isPressed());
        float f = getMovementMultiplier(this.playerInput.forward(), this.playerInput.backward());
        float g = getMovementMultiplier(this.playerInput.left(), this.playerInput.right());
        this.movementVector = (new Vec2f(g, f)).normalize();
    }
}
