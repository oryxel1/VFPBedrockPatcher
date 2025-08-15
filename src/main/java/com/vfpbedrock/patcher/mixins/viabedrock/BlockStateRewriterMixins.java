package com.vfpbedrock.patcher.mixins.viabedrock;

import com.llamalad7.mixinextras.sugar.Local;
import com.viaversion.nbt.tag.CompoundTag;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;


@Mixin(value = BlockStateRewriter.class, remap = false)
public class BlockStateRewriterMixins {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/viaversion/nbt/tag/CompoundTag;putInt(Ljava/lang/String;I)V"))
    public void cacheCustomBlocks(CompoundTag instance, String tagName, int value, @Local Map.Entry<String, CompoundTag> blockProperty) {
        instance.putInt(tagName, value);
        final CompoundTag tag = blockProperty.getValue();
        if (!tag.contains("components")) {
            return;
        }

        final CompoundTag components = tag.getCompoundTag("components");
        if (components.contains("minecraft:collision_box")) {
            instance.put("minecraft:collision_box", components.getCompoundTag("minecraft:collision_box"));
        }

        if (components.contains("minecraft:selection_box")) {
            instance.put("minecraft:selection_box", components.getCompoundTag("minecraft:selection_box"));
        }
    }
}
