package com.vfpbedrock.patcher.mixins.local.interaction;

import com.viaversion.viafabricplus.ViaFabricPlus;
import com.viaversion.viaversion.api.connection.UserConnection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.raphimc.viabedrock.api.BedrockProtocolVersion;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.AbilitiesIndex;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.GameSessionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixins {
    @Redirect(method = "attackBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isBlockBreakingRestricted(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/GameMode;)Z"))
    public boolean doBedrockProtectedArea(ClientPlayerEntity instance, World world, BlockPos blockPos, GameMode gameMode) {
        boolean old = instance.isBlockBreakingRestricted(world, blockPos, gameMode);
        if (((Object)this) != MinecraftClient.getInstance().player) {
            return old;
        }

        if (ViaFabricPlus.getImpl().getTargetVersion() != BedrockProtocolVersion.bedrockLatest) {
            return old;
        }

        final UserConnection user = ViaFabricPlus.getImpl().getPlayNetworkUserConnection();
        if (user == null) {
            return old;
        }

        final GameSessionStorage gameSession = user.get(GameSessionStorage.class);
        final net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity clientPlayer = user.get(EntityTracker.class).getClientPlayer();
        return !gameSession.isImmutableWorld() && clientPlayer.abilities().getBooleanValue(AbilitiesIndex.Mine);
    }
}
