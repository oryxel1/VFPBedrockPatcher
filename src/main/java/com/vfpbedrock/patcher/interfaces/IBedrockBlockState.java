package com.vfpbedrock.patcher.interfaces;

import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

public interface IBedrockBlockState {
    void vfpbedrockpatcher$collisionBox(VoxelShape box);
    void vfpbedrockpatcher$selectionBox(VoxelShape box);
    VoxelShape vfpbedrockpatcher$collisionBox();
    VoxelShape vfpbedrockpatcher$selectionBox();
}
