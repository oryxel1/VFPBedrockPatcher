package com.vfpbedrock.patcher.protocol.model;

import net.raphimc.viabedrock.protocol.data.enums.bedrock.InventorySourceType;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.InventorySource_InventorySourceFlags;

public record InventorySource(InventorySourceType type, int containerId, InventorySource_InventorySourceFlags flags) {
}