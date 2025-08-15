package com.vfpbedrock.patcher.mixins.viabedrock;

import com.vfpbedrock.patcher.protocol.enums.ClientInputLocksFlag;
import com.vfpbedrock.patcher.protocol.packets.ItemPackets;
import com.vfpbedrock.patcher.protocol.packets.VehiclePackets;
import com.vfpbedrock.patcher.tracker.*;
import com.viaversion.viaversion.api.connection.UserConnection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;
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
        VehiclePackets.register(protocol);

        protocol.registerClientbound(ClientboundBedrockPackets.UPDATE_CLIENT_INPUT_LOCKS, null, wrapper -> {
            wrapper.cancel();

            wrapper.user().get(InputTracker.class).flags(ClientInputLocksFlag.fromBitSet(wrapper.read(BedrockTypes.UNSIGNED_VAR_INT)));
            Position3f position3f = wrapper.read(BedrockTypes.POSITION_3F);

            final ClientPlayerEntity entity = MinecraftClient.getInstance().player;
            if (entity == null) {
                return;
            }

            entity.setPosition(new Vec3d(position3f.x(), position3f.y() - 1.62F, position3f.z()));
        });
    }

    @Inject(method = "init", at = @At("TAIL"))
    public void putOtherStoredObjects(UserConnection user, CallbackInfo ci) {
        user.put(new TeleportTracker(user));
        user.put(new VehicleTracker(user));
        user.put(new InputTracker(user));
    }
}
