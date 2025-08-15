package com.vfpbedrock.patcher.protocol.packets;

import com.vfpbedrock.patcher.tracker.VehicleTracker;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_21_5to1_21_6.packet.ClientboundPackets1_21_6;
import net.raphimc.viabedrock.api.model.entity.Entity;
import net.raphimc.viabedrock.protocol.BedrockProtocol;
import net.raphimc.viabedrock.protocol.ClientboundBedrockPackets;
import net.raphimc.viabedrock.protocol.data.enums.bedrock.ActorLinkType;
import net.raphimc.viabedrock.protocol.model.EntityLink;
import net.raphimc.viabedrock.protocol.storage.EntityTracker;
import net.raphimc.viabedrock.protocol.types.BedrockTypes;

import java.util.HashSet;
import java.util.Set;

public class VehiclePackets {
    public static void register(BedrockProtocol protocol) {
        protocol.registerClientbound(ClientboundBedrockPackets.SET_ENTITY_LINK, ClientboundPackets1_21_6.SET_PASSENGERS, wrapper -> {
            final EntityTracker entityTracker = wrapper.user().get(EntityTracker.class);
            final VehicleTracker vehicleTracker = wrapper.user().get(VehicleTracker.class);

            final EntityLink entityLink = wrapper.read(BedrockTypes.ENTITY_LINK);
            final ActorLinkType actorLinkType = ActorLinkType.getByValue(entityLink.type());

            final Entity entity = entityTracker.getEntityByUid(entityLink.fromUniqueEntityId());
            if (entity == null) {
                wrapper.cancel();
                return;
            }

            wrapper.write(Types.VAR_INT, entity.javaId()); // What entity to ride on?

            Set<Long> riders = vehicleTracker.entityToRiders().computeIfAbsent(entity, k -> new HashSet<>());

            boolean player = entityLink.toUniqueEntityId() == entityTracker.getClientPlayer().uniqueId();
            if (actorLinkType == ActorLinkType.None) {
                riders.remove(entityLink.toUniqueEntityId());
                if (player) {
                    vehicleTracker.vehicleRuntimeId(-1);
                }
            } else {
                riders.add(entityLink.toUniqueEntityId());
                if (player) {
                    vehicleTracker.vehicleRuntimeId(entityLink.toUniqueEntityId());
                }
            }

            riders.removeIf(l -> entityTracker.getEntityByUid(l) == null);

            int[] ridersJava = new int[riders.size()];
            int i = 0;
            for (Long l : riders) {
                ridersJava[i] = entityTracker.getEntityByUid(l).javaId();
                i++;
            }

            wrapper.write(Types.VAR_INT_ARRAY_PRIMITIVE, ridersJava);
        }, true);
    }
}
