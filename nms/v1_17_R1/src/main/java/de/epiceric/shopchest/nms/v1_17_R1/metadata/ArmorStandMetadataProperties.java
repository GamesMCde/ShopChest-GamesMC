package de.epiceric.shopchest.nms.v1_17_R1.metadata;

import de.epiceric.shopchest.nms.metadata.MetadataProperties;
import de.epiceric.shopchest.nms.metadata.MetadataProperty;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.decoration.ArmorStand;

public class ArmorStandMetadataProperties implements MetadataProperties.ArmorStand {

    private final static byte MARKER_FLAG = 0b10000;

    @Override
    public MetadataProperty<Boolean> marker() {
        return value -> (ExplicitMetadataValue) () -> new SynchedEntityData.DataItem<>(ArmorStand.DATA_CLIENT_FLAGS, value ? MARKER_FLAG : 0);
    }

}
