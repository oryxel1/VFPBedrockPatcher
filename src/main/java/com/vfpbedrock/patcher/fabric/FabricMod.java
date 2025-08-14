package com.vfpbedrock.patcher.fabric;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viafabricplus.api.events.LoadingCycleCallback;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.fabricmc.api.ModInitializer;
import net.lenni0451.reflect.stream.RStream;
import net.lenni0451.reflect.stream.field.FieldWrapper;
import net.raphimc.viabedrock.api.BedrockProtocolVersion;

public class FabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ViaFabricPlus.getImpl().registerLoadingCycleCallback(cycle -> {
            if (cycle == LoadingCycleCallback.LoadingCycle.FINAL_LOAD) {
                final ProtocolVersion bedrockLatest = RStream.of(BedrockProtocolVersion.class).fields().by("bedrockLatest").get();
                final FieldWrapper name = RStream.of(bedrockLatest).withSuper().fields().by("name");
                name.set(name.get() + " (Patched)");
            }
        });
    }
}
