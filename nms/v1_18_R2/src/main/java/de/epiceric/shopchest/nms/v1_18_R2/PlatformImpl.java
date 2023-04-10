package de.epiceric.shopchest.nms.v1_18_R2;

import de.epiceric.shopchest.nms.*;
import de.epiceric.shopchest.nms.network.entity.FakeArmorStand;
import de.epiceric.shopchest.nms.network.entity.FakeItem;
import de.epiceric.shopchest.nms.network.entity.metadata.MetadataProperties;
import de.epiceric.shopchest.nms.network.PacketQueue;
import de.epiceric.shopchest.nms.v1_18_R2.network.entity.FakeArmorStandImpl;
import de.epiceric.shopchest.nms.v1_18_R2.network.entity.FakeItemImpl;
import de.epiceric.shopchest.nms.v1_18_R2.network.entity.metadata.MetadataPropertiesImpl;
import de.epiceric.shopchest.nms.v1_18_R2.network.PacketQueueImpl;

public class PlatformImpl implements Platform {

    @Override
    public FakeArmorStand createFakeArmorStand() {
        return new FakeArmorStandImpl();
    }

    @Override
    public FakeItem createFakeItem() {
        return new FakeItemImpl();
    }

    @Override
    public PacketQueue createPacketQueue() {
        return new PacketQueueImpl();
    }

    @Override
    public TextComponentHelper getTextComponentHelper() {
        return new TextComponentHelperImpl();
    }

    @Override
    public MetadataProperties getMetadataProperties() {
        return new MetadataPropertiesImpl();
    }

}
