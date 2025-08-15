package com.vfpbedrock.patcher.mixins.viabedrock;

import com.vfpbedrock.patcher.interfaces.IBedrockContainer;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.model.container.Container;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ActorDataIDs;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ActorFlags;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerType;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.logging.Level;

@Mixin(value = InventoryTracker.class, remap = false)
public class InventoryTrackerMixins extends StoredObject {
    @Shadow
    private Container currentContainer = null;

    public InventoryTrackerMixins(UserConnection user) {
        super(user);
    }

    @Shadow
    private void forceCloseCurrentContainer() {}

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void allowForEntityBasedInventory(CallbackInfo ci) {
        ci.cancel();
        if (this.currentContainer == null) {
            return;
        }

        final EntityTracker entityTracker = this.user().get(EntityTracker.class);
        final ClientPlayerEntity clientPlayer = entityTracker.getClientPlayer();

        if (clientPlayer.entityFlags().contains(ActorFlags.SLEEPING)) {
            ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Closing " + this.currentContainer.type() + " because player have sleeping flags.");
            this.forceCloseCurrentContainer();
            return;
        }

        if (this.currentContainer.type() == ContainerType.INVENTORY) return;
        final Position3f playerPosition = clientPlayer.position();

        IBedrockContainer iContainer = (IBedrockContainer) this.currentContainer;
        final Entity attachedEntity = iContainer.vfpbedrockpatcher$uniqueEntityId() == -1 ? null : entityTracker.getEntityByUid(
                iContainer.vfpbedrockpatcher$uniqueEntityId());
        if (attachedEntity != null) {
            if (!attachedEntity.entityData().containsKey(ActorDataIDs.CONTAINER_SIZE)) {
                ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Closing " + this.currentContainer.type() + " because entity don't have container size metadata.");
                this.forceCloseCurrentContainer();
                return;
            }

            if (playerPosition.distanceTo(attachedEntity.position()) > 6) {
                ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Closing " + this.currentContainer.type() + " because player is too far away (" + playerPosition.distanceTo(attachedEntity.position()) + " > 6)");
                this.forceCloseCurrentContainer();
            }
        } else if (this.currentContainer.position() != null) {
            final ChunkTracker chunkTracker = this.user().get(ChunkTracker.class);
            final BlockStateRewriter blockStateRewriter = this.user().get(BlockStateRewriter.class);
            final int blockState = chunkTracker.getBlockState(this.currentContainer.position());
            final String tag = blockStateRewriter.tag(blockState);
            if (!this.currentContainer.isValidBlockTag(tag)) {
                ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Closing " + this.currentContainer.type() + " because block state is not valid for container type: " + blockState);
                this.forceCloseCurrentContainer();
                return;
            }

            final Position3f containerPosition = new Position3f(this.currentContainer.position().x() + 0.5F, this.currentContainer.position().y() + 0.5F, this.currentContainer.position().z() + 0.5F);
            if (playerPosition.distanceTo(containerPosition) > 6) {
                ViaBedrock.getPlatform().getLogger().log(Level.INFO, "Closing " + this.currentContainer.type() + " because player is too far away (" + playerPosition.distanceTo(containerPosition) + " > 6)");
                this.forceCloseCurrentContainer();
            }
        }
    }
}
