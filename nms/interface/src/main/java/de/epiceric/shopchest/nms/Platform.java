package de.epiceric.shopchest.nms;

/**
 * The platform that create all nms objects
 */
public interface Platform {

    /**
     * Create a {@link FakeArmorStand} object
     *
     * @return a new {@link FakeArmorStand}
     */
    FakeArmorStand createFakeArmorStand();

    /**
     * Create a {@link FakeItem} object
     *
     * @return a new {@link FakeItem}
     */
    FakeItem createFakeItem();

    /**
     * Create a {@link PacketQueue} object
     *
     * @return a new {@link PacketQueue}
     */
    PacketQueue createPacketQueue();


    TextComponentHelper getTextComponentHelper();

}
