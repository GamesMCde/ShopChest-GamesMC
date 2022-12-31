package de.epiceric.shopchest.nms.v1_19_R2;

import de.epiceric.shopchest.nms.FakeEntity;
import de.epiceric.shopchest.nms.PacketQueue;
import de.epiceric.shopchest.nms.ReflectionUtils;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class FakeEntityImpl implements FakeEntity {

    private final static AtomicInteger ENTITY_COUNTER;
    private final static EntityDataAccessor<Boolean> DATA_NO_GRAVITY;
    private final static EntityDataAccessor<Boolean> DATA_SILENT;

    static {
        try {
            ENTITY_COUNTER = (AtomicInteger) ReflectionUtils.getPrivateStaticFieldValue(Entity.class, "c");
            DATA_NO_GRAVITY = ReflectionUtils.forceCast(ReflectionUtils.getPrivateStaticFieldValue(Entity.class, "aP"));
            DATA_SILENT = ReflectionUtils.forceCast(ReflectionUtils.getPrivateStaticFieldValue(Entity.class, "aO"));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private final int entityId;

    public FakeEntityImpl() {
        entityId = ENTITY_COUNTER.incrementAndGet();
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public void create(PacketQueue packetQueue, UUID uuid, Location location) {
        final ClientboundAddEntityPacket addPacket = new ClientboundAddEntityPacket(
                entityId,
                uuid,
                location.getX(),
                location.getY(),
                location.getZ(),
                0f,
                0f,
                getEntityType(),
                0,
                Vec3.ZERO,
                0d
        );
        ((PacketQueueImpl) packetQueue).register(addPacket);
    }

    protected abstract EntityType<?> getEntityType();

    @Override
    public void remove(PacketQueue packetQueue) {
        final ClientboundRemoveEntitiesPacket removePacket = new ClientboundRemoveEntitiesPacket(entityId);
        ((PacketQueueImpl) packetQueue).register(removePacket);
    }

    @Override
    public void teleport(PacketQueue packetQueue, Vector position) {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeVarInt(entityId);
        buffer.writeDouble(position.getX());
        buffer.writeDouble(position.getY());
        buffer.writeDouble(position.getZ());
        buffer.writeByte(0);
        buffer.writeByte(0);
        buffer.writeBoolean(false);
        final ClientboundTeleportEntityPacket positionPacket = new ClientboundTeleportEntityPacket(buffer);
        ((PacketQueueImpl) packetQueue).register(positionPacket);
    }

    /**
     * Register a 'metadata' packet in the {@link PacketQueue} with the silent and no gravity properties
     *
     * @param packetQueue   The {@link PacketQueue} to store the packet
     * @param addProperties A {@link List} of {@link net.minecraft.network.syncher.SynchedEntityData.DataValue} to add
     */
    public void metadata(PacketQueue packetQueue, List<SynchedEntityData.DataValue<?>> addProperties) {
        final List<SynchedEntityData.DataValue<?>> packedItems = new LinkedList<>();
        packedItems.add(SynchedEntityData.DataValue.create(DATA_NO_GRAVITY, true));
        packedItems.add(SynchedEntityData.DataValue.create(DATA_SILENT, true));
        packedItems.addAll(addProperties);

        final ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(entityId, packedItems);
        ((PacketQueueImpl) packetQueue).register(dataPacket);
    }

}
