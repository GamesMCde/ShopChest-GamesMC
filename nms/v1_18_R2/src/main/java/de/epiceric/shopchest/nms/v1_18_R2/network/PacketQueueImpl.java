package de.epiceric.shopchest.nms.v1_18_R2.network;

import de.epiceric.shopchest.nms.network.PacketQueue;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerPlayerConnection;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

public class PacketQueueImpl implements PacketQueue {

    private final List<Packet<?>> packets;

    public PacketQueueImpl() {
        packets = new LinkedList<>();
    }

    public void register(Packet<?> packet) {
        packets.add(packet);
    }

    @Override
    public void send(Iterable<Player> receivers) {
        for (Player player : receivers) {
            final ServerPlayerConnection connection = ((CraftPlayer) player).getHandle().connection;
            for (Packet<?> packet : packets) {
                connection.send(packet);
            }
        }
    }
}
