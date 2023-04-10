package de.epiceric.shopchest.nms.v1_18_R2.network.entity;

import de.epiceric.shopchest.nms.network.entity.FakeItem;
import net.minecraft.world.entity.EntityType;

public class FakeItemImpl extends FakeEntityImpl implements FakeItem {

    public FakeItemImpl() {
        super();
    }

    @Override
    protected EntityType<?> getEntityType() {
        return EntityType.ITEM;
    }
}
