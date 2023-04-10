package de.epiceric.shopchest.nms.reflection;

import de.epiceric.shopchest.debug.DebugLogger;
import de.epiceric.shopchest.nms.network.entity.FakeArmorStand;

public class FakeArmorStandImpl extends FakeEntityImpl implements FakeArmorStand {

    public FakeArmorStandImpl(DebugLogger debug) {
        super(debug);
    }

    @Override
    protected Object getEntityType() {
        return null;
    }

    /*
    private final Class<?> packetPlayOutEntityTeleportClass = nmsClassResolver.resolveSilent("network.protocol.game.PacketPlayOutEntityTeleport");

    @Override
    public void sendData(String name, Iterable<Player> receivers) {
        Object dataWatcher = ReflectionUtils.createDataWatcher(debug, name, null);
        try {
            for (Player receiver : receivers) {
                ReflectionUtils.sendPacket(debug, packetPlayOutEntityMetadataClass.getConstructor(int.class, dataWatcherClass, boolean.class)
                        .newInstance(entityId, dataWatcher, true), receiver);
            }
        } catch (ReflectiveOperationException e) {
            debug.getLogger().severe("Could not set hologram data");
            debug.debug("Could not set armor stand data");
            debug.debug(e);
        }
    }

    @Override
    public void setLocation(Location location, Iterable<Player> receivers) {
        double y = location.getY() + (ReflectionUtils.getServerVersion().equals("v1_8_R1") ? 0 : 1.975);
        try {
            Object packet = packetPlayOutEntityTeleportClass.getConstructor().newInstance();
            Field[] fields = packetPlayOutEntityTeleportClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
            }

            boolean isPre9 = ReflectionUtils.getMajorVersion() < 9;
            fields[0].set(packet, entityId);

            if (isPre9) {
                fields[1].set(packet, (int)(location.getX() * 32));
                fields[2].set(packet, (int)(y * 32));
                fields[3].set(packet, (int)(location.getZ() * 32));
            } else {
                fields[1].set(packet, location.getX());
                fields[2].set(packet, y);
                fields[3].set(packet, location.getZ());
            }
            fields[4].set(packet, (byte) 0);
            fields[5].set(packet, (byte) 0);
            fields[6].set(packet, true);

            if (packet == null) {
                debug.getLogger().severe("Could not set hologram location");
                debug.debug("Could not set armor stand location: Packet is null");
                return;
            }

            for (Player receiver : receivers) {
                ReflectionUtils.sendPacket(debug, packet, receiver);
            }
        }catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void spawn(UUID uuid, Location location, Iterable<Player> receivers) {
        for(Player receiver : receivers) {
            ReflectionUtils.sendPacket(debug, ReflectionUtils.createPacketSpawnEntity(debug, entityId, uuid, location, EntityType.ARMOR_STAND), receiver);
        }
    }*/

}
