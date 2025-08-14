package com.vfpbedrock.patcher.protocol.packets;

import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ServerboundPackets1_21_6;
import net.raphimc.viabedrock.api.model.container.player.InventoryContainer;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ServerboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ItemUseInventoryTransaction_PredictedResult;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ItemUseInventoryTransaction_TriggerType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.generated.ComplexInventoryTransaction_Type;
import net.raphimc.viabedrock.protocol.data.enums.java.InteractionHand;
import net.raphimc.viabedrock.protocol.model.Position3f;
import net.raphimc.viabedrock.protocol.rewriter.ItemRewriter;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.storage.InventoryTracker;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class ItemPackets {
    public static void register(BedrockProtocol protocol) {
        protocol.registerServerbound(ServerboundPackets1_21_6.USE_ITEM, ServerboundBedrockPackets.INVENTORY_TRANSACTION, wrapper -> {
            // TODO: Implement armor swapping

            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final InventoryContainer inventoryContainer = wrapper.user().get(InventoryTracker.class).getInventoryContainer();

            final int hand = wrapper.read(Types.VAR_INT); // hand
            wrapper.read(Types.VAR_INT); // sequence
            wrapper.read(Types.FLOAT); // yaw
            wrapper.read(Types.FLOAT); // pitch

            // Bedrock can't hold the majority of item in offhand and can't use any either.
            if (hand != InteractionHand.MAIN_HAND.ordinal()) {
                wrapper.cancel();
                return;
            }

            wrapper.write(BedrockTypes.VAR_INT, 0); // legacy request id
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ComplexInventoryTransaction_Type.ItemUseTransaction.getValue()); // transaction type
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, 0); // actions count

            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, 1); // action type
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ItemUseInventoryTransaction_TriggerType.Unknown.getValue()); // trigger type

            wrapper.write(BedrockTypes.BLOCK_POSITION, new BlockPosition(0, 0, 0)); // block position

            // When player isn't right-clicking a block, block face always default back to 255
            wrapper.write(BedrockTypes.VAR_INT, 255); // Block face

            wrapper.write(BedrockTypes.VAR_INT, (int) inventoryContainer.getSelectedHotbarSlot()); // hotbar slot
            wrapper.write(wrapper.user().get(ItemRewriter.class).itemType(), inventoryContainer.getSelectedHotbarItem()); // hand item
            wrapper.write(BedrockTypes.POSITION_3F, entityTracker.getClientPlayer().position()); // player position
            wrapper.write(BedrockTypes.POSITION_3F, Position3f.ZERO); // Click position

            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, 0); // block runtime id
            wrapper.write(BedrockTypes.UNSIGNED_VAR_INT, ItemUseInventoryTransaction_PredictedResult.Failure.getValue()); // predicted result.
        });
    }
}
