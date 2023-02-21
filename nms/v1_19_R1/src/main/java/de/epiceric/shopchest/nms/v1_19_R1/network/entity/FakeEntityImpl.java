package de.epiceric.shopchest.nms.v1_19_R1.network.entity;

import de.epiceric.shopchest.nms.network.entity.FakeEntity;
import de.epiceric.shopchest.nms.network.PacketQueue;
import de.epiceric.shopchest.nms.ReflectionUtils;
import de.epiceric.shopchest.nms.network.entity.metadata.MetadataValue;
import de.epiceric.shopchest.nms.v1_19_R1.network.entity.metadata.ExplicitMetadataValue;
import de.epiceric.shopchest.nms.v1_19_R1.network.PacketQueueImpl;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class FakeEntityImpl implements FakeEntity {

    private final static AtomicInteger ENTITY_COUNTER;
    private final static Field packedItemField;

    static {
        try {
            ENTITY_COUNTER = (AtomicInteger) ReflectionUtils.getPrivateStaticFieldValue(Entity.class, "c");
            packedItemField = ClientboundSetEntityDataPacket.class.getDeclaredField("b"); // packedItems
            packedItemField.setAccessible(true);
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

    @Override
    public void metadata(PacketQueue packetQueue, MetadataValue[] values) {
        final List<SynchedEntityData.DataItem<?>> packedItems = Stream.of(values)
                .map(value -> ((ExplicitMetadataValue) value).toNMS())
                .collect(Collectors.toList());
        final SynchedEntityData entityData = new SynchedEntityData(null);
        final ClientboundSetEntityDataPacket dataPacket = new ClientboundSetEntityDataPacket(entityId, entityData, false);

        try {
            packedItemField.set(dataPacket, packedItems);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        ((PacketQueueImpl) packetQueue).register(dataPacket);
    }

    @Override
    public void cancelVelocity(PacketQueue packetQueue) {
        final ClientboundSetEntityMotionPacket velocityPacket = new ClientboundSetEntityMotionPacket(getEntityId(), Vec3.ZERO);
        ((PacketQueueImpl) packetQueue).register(velocityPacket);
    }

}
