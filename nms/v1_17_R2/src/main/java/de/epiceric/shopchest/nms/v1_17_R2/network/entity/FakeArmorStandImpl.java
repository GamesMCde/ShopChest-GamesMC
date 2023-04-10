package de.epiceric.shopchest.nms.v1_17_R2.network.entity;

import de.epiceric.shopchest.nms.network.entity.FakeArmorStand;
import net.minecraft.world.entity.EntityType;

public class FakeArmorStandImpl extends FakeEntityImpl implements FakeArmorStand {

    public FakeArmorStandImpl() {
        super();
    }

    @Override
    protected EntityType<?> getEntityType() {
        return EntityType.ARMOR_STAND;
    }
}
