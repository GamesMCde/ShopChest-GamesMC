package de.epiceric.shopchest.nms.v1_19_R2.network.entity.metadata;

import de.epiceric.shopchest.nms.ReflectionUtils;
import de.epiceric.shopchest.nms.network.entity.metadata.MetadataProperties;
import de.epiceric.shopchest.nms.network.entity.metadata.MetadataProperty;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.item.ItemEntity;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class ItemMetadataProperties implements MetadataProperties.Item {

    private final static EntityDataAccessor<net.minecraft.world.item.ItemStack> DATA_ITEM;

    static {
        try {
            DATA_ITEM = ReflectionUtils.forceCast(ReflectionUtils.getPrivateStaticFieldValue(ItemEntity.class, "c"));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MetadataProperty<ItemStack> item() {
        return value -> (ExplicitMetadataValue) () -> SynchedEntityData.DataValue.create(DATA_ITEM, CraftItemStack.asNMSCopy(value));
    }
}
