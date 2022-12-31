package de.epiceric.shopchest.nms.v1_19_R2;

import de.epiceric.shopchest.nms.*;

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

}
