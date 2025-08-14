package com.vfpbedrock.patcher.protocol;

import com.vfpbedrock.patcher.protocol.model.InventorySource;
import com.vfpbedrock.patcher.protocol.types.InventorySourceType;
import com.viaversion.viaversion.api.type.Type;

public class ExtraBedrockTypes {
    public static final Type<InventorySource> INVENTORY_SOURCE = new InventorySourceType();
}
