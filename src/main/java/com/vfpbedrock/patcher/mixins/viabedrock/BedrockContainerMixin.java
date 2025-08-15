package com.vfpbedrock.patcher.mixins.viabedrock;

import com.vfpbedrock.patcher.interfaces.IBedrockContainer;
import net.raphimc.viabedrock.api.model.container.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Container.class, remap = false)
public class BedrockContainerMixin implements IBedrockContainer {
    @Unique
    private long vfpbedrockpatcher$uniqueEntityId = -1;

    @Override
    public long vfpbedrockpatcher$uniqueEntityId() {
        return this.vfpbedrockpatcher$uniqueEntityId;
    }

    @Override
    public void vfpbedrockpatcher$uniqueEntityId(long id) {
        this.vfpbedrockpatcher$uniqueEntityId = id;
    }
}
