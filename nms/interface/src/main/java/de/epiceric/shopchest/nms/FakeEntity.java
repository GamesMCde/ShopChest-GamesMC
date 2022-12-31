package de.epiceric.shopchest.nms;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * Represent an entity that only exists clientside
 */
public interface FakeEntity {

    /**
     * Get entity id
     *
     * @return The entity id
     */
    int getEntityId();

    /**
     * Register a 'create' packet in the {@link PacketQueue}
     *
     * @param packetQueue The {@link PacketQueue} to store the packet
     * @param uuid        The {@link UUID} of the entity
     * @param location    The {@link Location} to spawn the entity
     */
    void create(PacketQueue packetQueue, UUID uuid, Location location);

    /**
     * Register a 'remove' packet in the {@link PacketQueue}
     *
     * @param packetQueue The {@link PacketQueue} to store the packet
     */
    void remove(PacketQueue packetQueue);

    /**
     * Register a 'teleport' packet in the {@link PacketQueue}
     *
     * @param packetQueue The {@link PacketQueue} to store the packet
     * @param position    The position to teleport the entity
     */
    void teleport(PacketQueue packetQueue, Vector position);

}
