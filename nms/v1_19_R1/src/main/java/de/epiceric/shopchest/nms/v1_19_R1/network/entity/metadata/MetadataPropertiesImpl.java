package de.epiceric.shopchest.nms.v1_19_R1.network.entity.metadata;

import de.epiceric.shopchest.nms.network.entity.metadata.MetadataProperties;

public class MetadataPropertiesImpl implements MetadataProperties {

    @Override
    public Entity entity() {
        return new EntityMetadataProperties();
    }

    @Override
    public ArmorStand armorStand() {
        return new ArmorStandMetadataProperties();
    }

    @Override
    public Item item() {
        return new ItemMetadataProperties();
    }
}
