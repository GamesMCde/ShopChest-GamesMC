package de.epiceric.shopchest.nms.v1_17_R1;

import de.epiceric.shopchest.nms.FakeItem;
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
