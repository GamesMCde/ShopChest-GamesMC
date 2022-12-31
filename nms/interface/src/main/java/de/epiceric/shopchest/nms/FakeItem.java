package de.epiceric.shopchest.nms;

import org.bukkit.inventory.ItemStack;

/**
 * Represent an Item entity that only exists clientside
 */
public interface FakeItem extends FakeEntity {

    /**
     * Register a 'metadata' packet in the {@link PacketQueue}
     * <br>
     * It sets the type of item
     *
     * @param packetQueue The {@link PacketQueue} to store the packet
     * @param item        The {@link ItemStack} type
     */
    void metadata(PacketQueue packetQueue, ItemStack item);

    /**
     * Register a zero 'velocity' packet in the {@link PacketQueue} to stop the item from moving
     *
     * @param packetQueue The {@link PacketQueue} to store the packet
     */
    void cancelVelocity(PacketQueue packetQueue);

}
