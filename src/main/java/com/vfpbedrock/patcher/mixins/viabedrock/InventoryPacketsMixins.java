package com.vfpbedrock.patcher.mixins.viabedrock;

import com.vfpbedrock.patcher.interfaces.IBedrockContainer;
import com.viaversion.nbt.tag.StringTag;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.components.TranslationComponent;
import net.raphimc.viabedrock.ViaBedrock;
import net.raphimc.viabedrock.api.chunk.BedrockBlockEntity;
import net.raphimc.viabedrock.api.model.container.ChestContainer;
import net.raphimc.viabedrock.api.model.container.Container;
import net.raphimc.viabedrock.api.model.container.player.InventoryContainer;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.api.util.PacketFactory;
import net.raphimc.viabedrock.api.util.TextUtil;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ActorDataIDs;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerID;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ContainerType;
import net.raphimc.viabedrock.protocol.packet.InventoryPackets;
import net.raphimc.viabedrock.protocol.rewriter.BlockStateRewriter;
import net.raphimc.viabedrock.protocol.storage.ChunkTracker;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;
import net.raphimc.viabedrock.protocol.storage.ResourcePacksStorage;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.logging.Level;

@Mixin(value = InventoryPackets.class, remap = false)
public class InventoryPacketsMixins {
    @Inject(method = "lambda$register$0", at = @At("HEAD"), cancellable = true)
    private static void allowForEntityBasedInventory(PacketWrapper wrapper, CallbackInfo ci) {
        ci.cancel();

        final ChunkTracker chunkTracker = wrapper.user().get(ChunkTracker.class);
        final BlockStateRewriter blockStateRewriter = wrapper.user().get(BlockStateRewriter.class);
        final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);
        final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
        byte containerId = wrapper.read(Types.BYTE); // container id
        final byte rawType = wrapper.read(Types.BYTE); // type
        final ContainerType type = ContainerType.getByValue(rawType);
        if (type == null) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Unknown ContainerType: " + rawType);
            wrapper.cancel();
            return;
        }
        final BlockPosition position = wrapper.read(BedrockTypes.BLOCK_POSITION); // position
        long uniqueEntityId = wrapper.read(BedrockTypes.VAR_LONG); // unique entity id

        if (inventoryTracker.isAnyScreenOpen()) {
            ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Server tried to open container while another container is open");
            PacketFactory.sendBedrockContainerClose(wrapper.user(), (byte) -1, ContainerType.NONE);
            wrapper.cancel();
            return;
        }
        final BedrockBlockEntity blockEntity = chunkTracker.getBlockEntity(position);
        TextComponent title = new TranslationComponent("container." + blockStateRewriter.tag(chunkTracker.getBlockState(position)));
        if (blockEntity != null && blockEntity.tag().get("CustomName") instanceof StringTag customNameTag) {
            title = TextUtil.stringToTextComponent(wrapper.user().get(ResourcePacksStorage.class).getTexts().translate(customNameTag.getValue()));
        }

        final Entity attachedEntity = uniqueEntityId == -1 ? null : entityTracker.getEntityByUid(uniqueEntityId);
        if (attachedEntity != null && attachedEntity.entityData().containsKey(ActorDataIDs.NAME)) {
            title = TextUtil.stringToTextComponent(wrapper.user().get(ResourcePacksStorage.class).getTexts().translate(attachedEntity.entityData().get(ActorDataIDs.NAME).value()));
        }

        final Container container;
        switch (type) {
            case INVENTORY -> {
                // Bedrock will always use the default id regardless of what the server send them.
                containerId = (byte) ContainerID.CONTAINER_ID_INVENTORY.getValue();

                inventoryTracker.setCurrentContainer(new InventoryContainer(wrapper.user(), containerId, position, inventoryTracker.getInventoryContainer()));
                wrapper.cancel();
                return;
            }
            case CONTAINER -> {
                container = new ChestContainer(wrapper.user(), containerId, title, position, 27);
                ((IBedrockContainer)container).vfpbedrockpatcher$uniqueEntityId(uniqueEntityId);
            }
            case NONE, CAULDRON, JUKEBOX, ARMOR, HAND, HUD, DECORATED_POT -> { // Bedrock client can't open these containers
                wrapper.cancel();
                return;
            }
            default -> {
                // throw new IllegalStateException("Unhandled ContainerType: " + type);
                wrapper.cancel();
                ViaBedrock.getPlatform().getLogger().log(Level.WARNING, "Tried to open unimplemented container: " + type);
                PacketFactory.sendBedrockContainerClose(wrapper.user(), containerId, ContainerType.NONE);
                return;
            }
        }
        inventoryTracker.setCurrentContainer(container);

        wrapper.write(Types.VAR_INT, (int) containerId); // container id
        wrapper.write(Types.VAR_INT, BedrockProtocol.MAPPINGS.getBedrockToJavaContainers().get(type)); // type
        wrapper.write(Types.TAG, TextUtil.textComponentToNbt(title)); // title
    }
}
