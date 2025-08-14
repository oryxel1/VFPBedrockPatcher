package com.vfpbedrock.patcher.mixins.viabedrock;

import net.raphimc.viabedrock.protocol.BedrockProtocol;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BedrockProtocol.class, remap = false)
public class BedrockProtocolMixin {
    @Inject(method = "registerPackets", at = @At("TAIL"))
    public void registerExtraPackets(CallbackInfo ci) {
    }
}
