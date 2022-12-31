package de.epiceric.shopchest.nms.v1_19_R2;

import de.epiceric.shopchest.nms.FakeItem;
import de.epiceric.shopchest.nms.PacketQueue;
import de.epiceric.shopchest.nms.ReflectionUtils;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class FakeItemImpl extends FakeEntityImpl implements FakeItem {

    private final static EntityDataAccessor<net.minecraft.world.item.ItemStack> DATA_ITEM;

    static {
        try {
            DATA_ITEM = ReflectionUtils.forceCast(ReflectionUtils.getPrivateStaticFieldValue(ItemEntity.class, "c"));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public FakeItemImpl() {
        super();
    }

    @Override
    public void metadata(PacketQueue packetQueue, ItemStack item) {
        final List<SynchedEntityData.DataValue<?>> addProperties = Collections.singletonList(
                SynchedEntityData.DataValue.create(DATA_ITEM, CraftItemStack.asNMSCopy(item))
        );
        metadata(packetQueue, addProperties);
    }

    @Override
    public void cancelVelocity(PacketQueue packetQueue) {
        final ClientboundSetEntityMotionPacket velocityPacket = new ClientboundSetEntityMotionPacket(getEntityId(), Vec3.ZERO);
        ((PacketQueueImpl) packetQueue).register(velocityPacket);
    }

    @Override
    protected EntityType<?> getEntityType() {
        return EntityType.ITEM;
    }
}
