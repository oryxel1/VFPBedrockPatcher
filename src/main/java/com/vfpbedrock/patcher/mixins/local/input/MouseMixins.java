package com.vfpbedrock.patcher.mixins.local.input;

import com.vfpbedrock.patcher.protocol.enums.ClientInputLocksFlag;
import com.vfpbedrock.patcher.tracker.InputTracker;
import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.connection.UserConnection;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.vfpbedrock.patcher.other.RandomMethods.canDoMixins;

@Mixin(Mouse.class)
public class MouseMixins {
    @Inject(method = "updateMouse", at = @At("HEAD"), cancellable = true)
    public void lockMouseWhenNeeded(double timeDelta, CallbackInfo ci) {
        if (!canDoMixins(this, true, false)) {
            return;
        }

        final UserConnection user = ViaFabricPlus.getImpl().getPlayNetworkUserConnection();
        if (user == null) {
            return;
        }

        if (user.get(InputTracker.class).has(ClientInputLocksFlag.CAMERA)) {
            ci.cancel();
        }
    }
}
