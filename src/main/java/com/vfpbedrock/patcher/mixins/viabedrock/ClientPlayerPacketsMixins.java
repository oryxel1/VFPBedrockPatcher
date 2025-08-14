package com.vfpbedrock.patcher.mixins.viabedrock;

import com.llamalad7.mixinextras.sugar.Local;
import com.vfpbedrock.patcher.protocol.ExtraBedrockTypes;
import com.vfpbedrock.patcher.protocol.model.InventoryAction;
import com.vfpbedrock.patcher.protocol.model.InventorySource;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import net.raphimc.viabedrock.api.model.container.player.InventoryContainer;
import net.raphimc.viabedrock.api.model.entity.ClientPlayerEntity;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerID;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.InventorySourceType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.InventorySource_InventorySourceFlags;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ComplexInventoryTransaction_Type;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.PlayerAuthInputPacket_InputData;
import net.raphimc.viabedrock.protocol.data.enums.java.PlayerActionAction;
import net.raphimc.viabedrock.protocol.model.BedrockItem;
import net.raphimc.viabedrock.protocol.packet.ClientPlayerPackets;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ClientPlayerPackets.class, remap = false)
public class ClientPlayerPacketsMixins {
    @Redirect(method = "lambda$register$17", at = @At(value = "INVOKE", target = "Lnet/raphimc/viabedrock" +
            "/api/model/entity/ClientPlayerEntity;addAuthInputData" +
            "(Lnet/raphimc/viabedrock/protocol/data/enums/bedrock/generated/PlayerAuthInputPacket_InputData;)V", ordinal = 0))
    private static void cancelStartJumping(ClientPlayerEntity instance, PlayerAuthInputPacket_InputData data) {
        // Don't do anything since we already handle this in LivingEntityMixins#addStartJumpingBedrock
    }

    @Inject(method = "lambda$register$9", at = @At(value = "INVOKE",
            target = "Lcom/viaversion/viaversion/api/protocol/packet/PacketWrapper;read(Lcom/viaversion/viaversion/api/type/Type;)Ljava/lang/Object;",
    ordinal = 3, shift = At.Shift.AFTER), cancellable = true)
    private static void translateItemDropRelease(PacketWrapper wrapper, CallbackInfo ci, @Local PlayerActionAction action) {
        if (action != PlayerActionAction.RELEASE_USE_ITEM && action != PlayerActionAction.DROP_ITEM && action != PlayerActionAction.DROP_ALL_ITEMS) {
            return;
        }

        final InventoryTracker inventoryTracker = wrapper.user().get(InventoryTracker.class);

        wrapper.setCancelled(false);

        // Ooops messy code :D, who cares.
        // For release item and drop item, both is the same regardless of the inventory auth mode.
        ci.cancel();
        if (action != PlayerActionAction.RELEASE_USE_ITEM) {
            wrapper.setPacketType(ServerboundBedrockPackets.INVENTORY_TRANSACTION);

            wrapper.write(BedrockTypes.VAR_INT, 0); // legacy request id
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ComplexInventoryTransaction_Type.NormalTransaction.getValue()); // transaction type

            final List<InventoryAction> actions = new ArrayList<>();

            int slot = inventoryTracker.getInventoryContainer().getSelectedHotbarSlot();
            BedrockItem thrownItem = inventoryTracker.getInventoryContainer().getSelectedHotbarItem();
            int throwAmount = action == PlayerActionAction.DROP_ITEM ? 1 : thrownItem.amount();

            BedrockItem worldThrownItem = thrownItem.copy();
            worldThrownItem.setAmount(throwAmount);

            actions.add(new InventoryAction(new InventorySource(InventorySourceType.WorldInteraction, ContainerID.CONTAINER_ID_NONE.getValue(), InventorySource_InventorySourceFlags.NoFlag), 0, BedrockItem.empty(), worldThrownItem));

            BedrockItem inventoryThrownItem = thrownItem.copy();
            if (thrownItem.amount() - throwAmount <= 0) {
                inventoryThrownItem = BedrockItem.empty();
            } else {
                inventoryThrownItem.setAmount(thrownItem.amount() - throwAmount);
            }

            inventoryTracker.getInventoryContainer().setItem(slot, inventoryThrownItem);
            actions.add(new InventoryAction(new InventorySource(InventorySourceType.ContainerInventory, ContainerID.CONTAINER_ID_INVENTORY.getValue(), InventorySource_InventorySourceFlags.NoFlag), slot, thrownItem, inventoryThrownItem));

            Type<BedrockItem> bedrockItemType = wrapper.user().get(ItemRewriter.class).itemType();

            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, actions.size()); // actions count
            for (InventoryAction inventoryAction : actions) {
                wrapper.write(ExtraBedrockTypes.INVENTORY_SOURCE, inventoryAction.source()); // inventory source
                wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, inventoryAction.slot()); // slot
                wrapper.write(bedrockItemType, inventoryAction.from());
                wrapper.write(bedrockItemType, inventoryAction.to());
            }
        } else {
            final InventoryContainer inventoryContainer = inventoryTracker.getInventoryContainer();

            wrapper.setPacketType(ServerboundBedrockPackets.INVENTORY_TRANSACTION);

            wrapper.write(BedrockTypes.VAR_INT, 0); // legacy request id
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ComplexInventoryTransaction_Type.ItemReleaseTransaction.getValue()); // transaction type
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, 0); // actions count
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, 0); // action type
            wrapper.write(BedrockTypes.VAR_INT, (int) inventoryContainer.getSelectedHotbarSlot()); // selected hotbar slot
            wrapper.write(wrapper.user().get(ItemRewriter.class).itemType(), inventoryContainer.getSelectedHotbarItem()); // hand item
            wrapper.write(BedrockTypes.POSITION_3F, wrapper.user().get(EntityTracker.class).getClientPlayer().position()); // head position, the same as player position.
        }
    }
}
