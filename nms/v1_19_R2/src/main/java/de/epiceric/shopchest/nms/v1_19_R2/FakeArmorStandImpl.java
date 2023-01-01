package de.epiceric.shopchest.nms.v1_19_R2;

import de.epiceric.shopchest.nms.FakeArmorStand;
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
