package com.vfpbedrock.patcher.mixins.viabedrock;

import com.vfpbedrock.patcher.interfaces.IBedrockBlockState;
import com.vfpbedrock.patcher.other.RandomMethods;
import com.viaversion.nbt.tag.CompoundTag;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.raphimc.viabedrock.api.model.BedrockBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BedrockBlockState.class, remap = false)
public class BedrockBlockStateMixins implements IBedrockBlockState {
    @Unique
    private VoxelShape vfpbedrockpatcher$collisionBox;
    @Unique
    private VoxelShape vfpbedrockpatcher$selectionBox;

    @Inject(method = "fromNbt", at = @At("TAIL"), cancellable = true)
    private static void cacheCollisionAndSelectionBox(CompoundTag tag, CallbackInfoReturnable<BedrockBlockState> cir) {
        BedrockBlockState state = cir.getReturnValue();
        System.out.println();
        if (tag.contains("minecraft:collision_box")) {
            final CompoundTag collision = tag.getCompoundTag("minecraft:collision_box");

            boolean enabled = true;
            if (collision.contains("enabled")) {
                enabled = collision.getBoolean("enabled");
            }

            if (enabled && collision.contains("origin") && collision.contains("size")) {
                ((IBedrockBlockState)state).vfpbedrockpatcher$collisionBox(RandomMethods.boxComponentToVoxel(collision));
            } else {
                ((IBedrockBlockState)state).vfpbedrockpatcher$collisionBox(VoxelShapes.cuboidUnchecked(0, 0, 0, 0, 0, 0));
            }
        }

        if (tag.contains("minecraft:selection_box")) {
            final CompoundTag collision = tag.getCompoundTag("minecraft:selection_box");

            boolean enabled = true;
            if (collision.contains("enabled")) {
                enabled = collision.getBoolean("enabled");
            }

            if (enabled && collision.contains("origin") && collision.contains("size")) {
                ((IBedrockBlockState)state).vfpbedrockpatcher$selectionBox(RandomMethods.boxComponentToVoxel(collision));
            } else {
                ((IBedrockBlockState)state).vfpbedrockpatcher$selectionBox(VoxelShapes.cuboidUnchecked(0, 0, 0, 0, 0, 0));
            }
        }

        System.out.println(tag.getStringTag("name") + " -> " + ((IBedrockBlockState)state).vfpbedrockpatcher$collisionBox());
    }

    @Override
    public void vfpbedrockpatcher$collisionBox(VoxelShape box) {
        this.vfpbedrockpatcher$collisionBox = box;
    }

    @Override
    public void vfpbedrockpatcher$selectionBox(VoxelShape box) {
        this.vfpbedrockpatcher$selectionBox = box;
    }

    @Override
    public VoxelShape vfpbedrockpatcher$collisionBox() {
        return this.vfpbedrockpatcher$collisionBox;
    }

    @Override
    public VoxelShape vfpbedrockpatcher$selectionBox() {
        return this.vfpbedrockpatcher$selectionBox;
    }
}
