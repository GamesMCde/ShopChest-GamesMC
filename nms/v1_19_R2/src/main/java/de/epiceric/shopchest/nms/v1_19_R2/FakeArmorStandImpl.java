package de.epiceric.shopchest.nms.v1_19_R2;

import de.epiceric.shopchest.nms.FakeArmorStand;
import de.epiceric.shopchest.nms.NMSComponent;
import de.epiceric.shopchest.nms.PacketQueue;
import de.epiceric.shopchest.nms.ReflectionUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class FakeArmorStandImpl extends FakeEntityImpl implements FakeArmorStand {

    private final static byte INVISIBLE_FLAG = 0b100000;
    private final static byte MARKER_FLAG = 0b10000;
    private final static EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID;
    private final static EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME;
    private final static EntityDataAccessor<Boolean> DATA_CUSTOM_NAME_VISIBLE;

    static {
        try {
            DATA_SHARED_FLAGS_ID = ReflectionUtils.forceCast(ReflectionUtils.getPrivateStaticFieldValue(Entity.class, "Z"));
            DATA_CUSTOM_NAME = ReflectionUtils.forceCast(ReflectionUtils.getPrivateStaticFieldValue(Entity.class, "aM"));
            DATA_CUSTOM_NAME_VISIBLE = ReflectionUtils.forceCast(ReflectionUtils.getPrivateStaticFieldValue(Entity.class, "aN"));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public FakeArmorStandImpl() {
        super();
    }

    @Override
    public void metadata(PacketQueue packetQueue, NMSComponent customName) {
        final List<SynchedEntityData.DataValue<?>> addProperties = Arrays.asList(
                SynchedEntityData.DataValue.create(DATA_SHARED_FLAGS_ID, INVISIBLE_FLAG),
                // TODO Handle customName properly
                SynchedEntityData.DataValue.create(DATA_CUSTOM_NAME, Optional.ofNullable(null)),
                SynchedEntityData.DataValue.create(DATA_CUSTOM_NAME_VISIBLE, true),
                SynchedEntityData.DataValue.create(ArmorStand.DATA_CLIENT_FLAGS, MARKER_FLAG)
        );
        super.metadata(packetQueue, addProperties);
    }

    @Override
    protected EntityType<?> getEntityType() {
        return EntityType.ARMOR_STAND;
    }
}
