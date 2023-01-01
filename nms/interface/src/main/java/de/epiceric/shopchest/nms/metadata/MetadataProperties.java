package de.epiceric.shopchest.nms.metadata;

import de.epiceric.shopchest.nms.NMSComponent;
import org.bukkit.inventory.ItemStack;

public interface MetadataProperties {

    Entity entity();

    ArmorStand armorStand();

    Item item();

    interface Entity {

        MetadataProperty<Boolean> noGravity();

        MetadataProperty<Boolean> silent();

        MetadataProperty<Boolean> invisible();

        MetadataProperty<NMSComponent> customName();

        MetadataProperty<Boolean> customNameVisible();
    }

    interface ArmorStand {
        MetadataProperty<Boolean> marker();
    }

    interface Item {
        MetadataProperty<ItemStack> item();
    }

}
