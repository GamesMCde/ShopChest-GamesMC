package de.epiceric.shopchest.nms.v1_21_R2;

import de.epiceric.shopchest.nms.FakeArmorStand;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_21_R2.util.CraftChatMessage;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

public class FakeArmorStandImpl extends FakeEntityImpl<String> implements FakeArmorStand {

    private final static byte INVISIBLE_FLAG = 0b100000;
    private final static byte MARKER_FLAG = 0b10000;
    private final static EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID;
    private final static EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME;
    private final static EntityDataAccessor<Boolean> DATA_CUSTOM_NAME_VISIBLE;
    private final static float MARKER_ARMOR_STAND_OFFSET = 1.975f;

    static {
        try {
            final Field dataSharedFlagsId = Entity.class.getDeclaredField(ObfuscatedFieldNames.DATA_SHARED_FLAGS_ID);
            dataSharedFlagsId.setAccessible(true);
            DATA_SHARED_FLAGS_ID = forceCast(dataSharedFlagsId.get(null));
            final Field dataCustomNameField = Entity.class.getDeclaredField(ObfuscatedFieldNames.DATA_CUSTOM_NAME);
            dataCustomNameField.setAccessible(true);
            DATA_CUSTOM_NAME = forceCast(dataCustomNameField.get(null));
            final Field dataCustomNameVisibleField = Entity.class.getDeclaredField(ObfuscatedFieldNames.DATA_CUSTOM_NAME_VISIBLE);
            dataCustomNameVisibleField.setAccessible(true);
            DATA_CUSTOM_NAME_VISIBLE = forceCast(dataCustomNameVisibleField.get(null));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public FakeArmorStandImpl() {
        super();
    }

    @Override
    public void sendData(String name, Iterable<Player> receivers) {
        sendData(receivers, name);
    }

    @Override
    protected EntityType<?> getEntityType() {
        return EntityType.ARMOR_STAND;
    }

    @Override
    protected float getSpawnOffSet() {
        return MARKER_ARMOR_STAND_OFFSET;
    }

    @Override
    protected int getDataItemCount() {
        return 4;
    }

    @Override
    protected void addSpecificData(List<SynchedEntityData.DataValue<?>> packedItems, String name) {
        packedItems.add(SynchedEntityData.DataValue.create(DATA_SHARED_FLAGS_ID, INVISIBLE_FLAG));
        packedItems.add(SynchedEntityData.DataValue.create(DATA_CUSTOM_NAME, Optional.ofNullable(
                CraftChatMessage.fromStringOrNull(name)
        )));
        packedItems.add(SynchedEntityData.DataValue.create(DATA_CUSTOM_NAME_VISIBLE, true));
        packedItems.add(SynchedEntityData.DataValue.create(ArmorStand.DATA_CLIENT_FLAGS, MARKER_FLAG));
    }

    @Override
    public void setLocation(Location location, Iterable<Player> receivers) {
        final FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeVarInt(entityId);
        buffer.writeDouble(location.getX());
        buffer.writeDouble(location.getY() + MARKER_ARMOR_STAND_OFFSET);
        buffer.writeDouble(location.getZ());
        buffer.writeByte(0);
        buffer.writeByte(0);
        buffer.writeBoolean(false);
        final ClientboundTeleportEntityPacket positionPacket = ClientboundTeleportEntityPacket.STREAM_CODEC.decode(buffer);
        sendPacket(positionPacket, receivers);
    }

}
