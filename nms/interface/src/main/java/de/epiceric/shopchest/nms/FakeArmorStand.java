package de.epiceric.shopchest.nms;

/**
 * Represent an ArmorStand that only exists clientside
 */
public interface FakeArmorStand extends FakeEntity {

    /**
     * Register a 'metadata' packet in the {@link PacketQueue}
     * <br>
     * It sets the invisibility, the custom name, make it visible and the marker flag
     *
     * @param packetQueue The {@link PacketQueue} to store the packet
     * @param customName  The name to set
     */
    void metadata(PacketQueue packetQueue, NMSComponent customName);

}
