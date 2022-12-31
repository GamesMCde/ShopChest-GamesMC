package de.epiceric.shopchest.nms.reflection;

import de.epiceric.shopchest.debug.DebugLogger;
import de.epiceric.shopchest.nms.FakeEntity;
import de.epiceric.shopchest.nms.PacketQueue;
import de.epiceric.shopchest.nms.metadata.MetadataValue;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.inventivetalent.reflection.resolver.minecraft.NMSClassResolver;

import java.util.UUID;

public abstract class FakeEntityImpl implements FakeEntity {

    /*

    protected final NMSClassResolver nmsClassResolver = new NMSClassResolver();
    protected final Class<?> packetPlayOutEntityDestroyClass = nmsClassResolver.resolveSilent("network.protocol.game.PacketPlayOutEntityDestroy");
    protected final Class<?> packetPlayOutEntityMetadataClass = nmsClassResolver.resolveSilent("network.protocol.game.PacketPlayOutEntityMetadata");
    protected final Class<?> dataWatcherClass = nmsClassResolver.resolveSilent("network.syncher.DataWatcher");
    */

    protected final int entityId;
    protected final DebugLogger debug;

    public FakeEntityImpl(DebugLogger debug) {
        this.entityId = ReflectionUtils.getFreeEntityId();
        this.debug = debug;
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public void create(PacketQueue packetQueue, UUID uuid, Location location) {

    }

    @Override
    public void remove(PacketQueue packetQueue) {

    }

    @Override
    public void teleport(PacketQueue packetQueue, Vector position) {

    }

    @Override
    public void metadata(PacketQueue packetQueue, MetadataValue... metadataValues) {

    }

    @Override
    public void cancelVelocity(PacketQueue packetQueue) {

    }

    protected abstract Object getEntityType();

    /*
    @Override
    public void remove(Iterable<Player> receivers) {
        try {
            for(Player receiver : receivers) {
                ReflectionUtils.sendPacket(debug, packetPlayOutEntityDestroyClass.getConstructor(int[].class).newInstance((Object) new int[]{entityId}), receiver);
            }
        } catch (ReflectiveOperationException e){
            debug.getLogger().severe("Could not remove hologram");
            debug.debug("Could not remove hologram");
            debug.debug(e);
        }
    }
    */
}
