package com.vfpbedrock.patcher.mixins.viabedrock;

import com.vfpbedrock.patcher.protocol.packets.ItemPackets;
import com.vfpbedrock.patcher.tracker.TeleportTracker;
import com.viaversion.viaversion.api.connection.UserConnection;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BedrockProtocol.class, remap = false)
public class BedrockProtocolMixin {
    @Inject(method = "registerPackets", at = @At("TAIL"))
    public void registerExtraPackets(CallbackInfo ci) {
        final BedrockProtocol protocol = (BedrockProtocol) (Object) this;
        ItemPackets.register(protocol);
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void putOtherStoredObjects(UserConnection user, CallbackInfo ci) {
        user.put(new TeleportTracker(user));
    }
}
