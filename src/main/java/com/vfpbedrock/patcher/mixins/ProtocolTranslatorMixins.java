package com.vfpbedrock.patcher.mixins;

import com.viaversion.viafabricplus.protocoltranslator.ProtocolTranslator;
import net.lenni0451.reflect.stream.field.FieldWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ProtocolTranslator.class, remap = false)
public class ProtocolTranslatorMixins {
    @Redirect(method = "changeBedrockProtocolName", at = @At(value = "INVOKE", target = "Lnet/lenni0451/reflect/stream/field/FieldWrapper;set(Ljava/lang/Object;)V"))
    private static void changeBedrockProtocolName(FieldWrapper instance, Object value) {
        instance.set(instance.get() + " (Patched)");
    }
}
