package de.epiceric.shopchest.nms;

import de.epiceric.shopchest.nms.network.entity.FakeArmorStand;
import de.epiceric.shopchest.nms.network.entity.FakeItem;
import de.epiceric.shopchest.nms.network.entity.metadata.MetadataProperties;
import de.epiceric.shopchest.nms.network.PacketQueue;

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

    /**
     * Get a list of {@link de.epiceric.shopchest.nms.network.entity.metadata.MetadataProperty}
     *
     * @return The {@link MetadataProperties} instance
     */
    MetadataProperties getMetadataProperties();

}
