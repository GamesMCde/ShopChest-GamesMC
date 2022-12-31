package de.epiceric.shopchest.nms.reflection;

import de.epiceric.shopchest.nms.PacketQueue;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public class PacketQueueImpl implements PacketQueue {

    private final List<Object> packets;

    public PacketQueueImpl() {
        packets = new LinkedList<>();
    }

    public void register(Object packet) {
        packets.add(packet);
    }

    @Override
    public void send(Iterable<Player> receivers) {
        /*
        for (Player player : receivers) {
            final ServerPlayerConnection connection = ((CraftPlayer) player).getHandle().connection;
            for (Packet<?> packet : packets) {
                connection.send(packet);
            }
        }*/
    }
}
