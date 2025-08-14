package com.vfpbedrock.patcher.protocol.types;

import com.vfpbedrock.patcher.protocol.model.InventorySource;
import com.viaversion.viaversion.api.type.Type;
import io.netty.buffer.ByteBuf;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ContainerID;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.InventorySource_InventorySourceFlags;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

public class InventorySourceType extends Type<InventorySource> {
    public InventorySourceType() {
        super(InventorySource.class);
    }

    @Override
    public InventorySource read(ByteBuf buffer) {
        int rawTypeId = BedrockTypes.UNSIGNED_VAR_INT.read(buffer);
        net.raphimc.viabedrock.protocol.data.enums.bedrock.InventorySourceType type = net.raphimc.viabedrock.protocol.data.enums.bedrock.InventorySourceType.getByValue(rawTypeId);
        if (type == null) {
            throw new IllegalStateException("Invalid inventory source type id: " + rawTypeId);
        }

        switch (type) {
            case ContainerInventory, NonImplementedFeatureTODO -> {
                return new InventorySource(type, BedrockTypes.VAR_INT.read(buffer), InventorySource_InventorySourceFlags.NoFlag);
            }
            case WorldInteraction -> {
                int rawSourceFlagId = BedrockTypes.UNSIGNED_VAR_INT.read(buffer);
                InventorySource_InventorySourceFlags flag = InventorySource_InventorySourceFlags.getByValue(rawSourceFlagId);
                if (flag == null) {
                    throw new IllegalStateException("Invalid inventory source flag id: " + rawSourceFlagId);
                }

                return new InventorySource(type, ContainerID.CONTAINER_ID_NONE.getValue(), flag);
            }
            default -> {
                return new InventorySource(type, ContainerID.CONTAINER_ID_NONE.getValue(), InventorySource_InventorySourceFlags.NoFlag);
            }
        }
    }

    @Override
    public void write(ByteBuf buffer, InventorySource value) {
        BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.type().getValue());

        switch (value.type()) {
            case ContainerInventory, NonImplementedFeatureTODO -> BedrockTypes.VAR_INT.write(buffer, value.containerId());
            case WorldInteraction -> BedrockTypes.UNSIGNED_VAR_INT.write(buffer, value.flags().getValue());
        }
    }
}