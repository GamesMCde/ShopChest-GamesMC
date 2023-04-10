package de.epiceric.shopchest.nms.v1_18_R1.network.entity.metadata;

import de.epiceric.shopchest.nms.NMSComponent;
import de.epiceric.shopchest.nms.ReflectionUtils;
import de.epiceric.shopchest.nms.network.entity.metadata.MetadataProperties;
import de.epiceric.shopchest.nms.network.entity.metadata.MetadataProperty;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

import java.util.Optional;

public class EntityMetadataProperties implements MetadataProperties.Entity {

    private final static EntityDataAccessor<Boolean> DATA_NO_GRAVITY;
    private final static EntityDataAccessor<Boolean> DATA_SILENT;
    private final static EntityDataAccessor<Byte> DATA_SHARED_FLAGS_ID;
    private final static EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME;
    private final static EntityDataAccessor<Boolean> DATA_CUSTOM_NAME_VISIBLE;
    private final static byte INVISIBLE_FLAG = 0b100000;

    static {
        try {
            DATA_NO_GRAVITY = ReflectionUtils.forceCast(ReflectionUtils.getPrivateStaticFieldValue(Entity.class, "aO"));
            DATA_SILENT = ReflectionUtils.forceCast(ReflectionUtils.getPrivateStaticFieldValue(Entity.class, "aN"));
            DATA_SHARED_FLAGS_ID = ReflectionUtils.forceCast(ReflectionUtils.getPrivateStaticFieldValue(Entity.class, "aa"));
            DATA_CUSTOM_NAME = ReflectionUtils.forceCast(ReflectionUtils.getPrivateStaticFieldValue(Entity.class, "aL"));
            DATA_CUSTOM_NAME_VISIBLE = ReflectionUtils.forceCast(ReflectionUtils.getPrivateStaticFieldValue(Entity.class, "aM"));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MetadataProperty<Boolean> noGravity() {
        return value -> (ExplicitMetadataValue) () -> new SynchedEntityData.DataItem<>(DATA_NO_GRAVITY, value);
    }

    @Override
    public MetadataProperty<Boolean> silent() {
        return value -> (ExplicitMetadataValue) () -> new SynchedEntityData.DataItem<>(DATA_SILENT, value);
    }

    @Override
    public MetadataProperty<Boolean> invisible() {
        return value -> (ExplicitMetadataValue) () -> new SynchedEntityData.DataItem<>(DATA_SHARED_FLAGS_ID, value ? INVISIBLE_FLAG : 0);
    }

    @Override
    public MetadataProperty<NMSComponent> customName() {
        // TODO Handle customName properly
        return value -> (ExplicitMetadataValue) () -> new SynchedEntityData.DataItem<>(DATA_CUSTOM_NAME, Optional.ofNullable(null));
    }

    @Override
    public MetadataProperty<Boolean> customNameVisible() {
        return value -> (ExplicitMetadataValue) () -> new SynchedEntityData.DataItem<>(DATA_CUSTOM_NAME_VISIBLE, true);
    }
}
