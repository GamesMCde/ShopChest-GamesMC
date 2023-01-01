package de.epiceric.shopchest.nms;

import org.bukkit.entity.Player;

/**
 * A queue of packet that can be sent
 */
public interface PacketQueue {

    /**
     * Send all packets to players
     *
     * @param receivers The {@link Player}s that receives the packets
     */
    void send(Iterable<Player> receivers);

}
