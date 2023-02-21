package de.epiceric.shopchest.nms.reflection;

import de.epiceric.shopchest.debug.DebugLogger;
import de.epiceric.shopchest.nms.*;
import de.epiceric.shopchest.nms.network.entity.FakeArmorStand;
import de.epiceric.shopchest.nms.network.entity.FakeItem;
import de.epiceric.shopchest.nms.network.entity.metadata.MetadataProperties;
import de.epiceric.shopchest.nms.network.PacketQueue;

public class PlatformImpl implements Platform {

    private final DebugLogger debug;

    public PlatformImpl(DebugLogger debug) {
        this.debug = debug;
    }

    @Override
    public FakeArmorStand createFakeArmorStand() {
        return new FakeArmorStandImpl(debug);
    }

    @Override
    public FakeItem createFakeItem() {
        return new FakeItemImpl(debug);
    }

    @Override
    public PacketQueue createPacketQueue() {
        return null;
    }

    @Override
    public TextComponentHelper getTextComponentHelper() {
        return new TextComponentHelperImpl(debug);
    }

    @Override
    public MetadataProperties getMetadataProperties() {
        return null;
    }


}
