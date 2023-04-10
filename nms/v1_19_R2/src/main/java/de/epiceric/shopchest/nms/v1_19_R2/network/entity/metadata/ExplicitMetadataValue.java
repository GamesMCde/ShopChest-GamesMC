package de.epiceric.shopchest.nms.v1_19_R2.network.entity.metadata;

import de.epiceric.shopchest.nms.network.entity.metadata.MetadataValue;
import net.minecraft.network.syncher.SynchedEntityData;

public interface ExplicitMetadataValue extends MetadataValue {

    SynchedEntityData.DataValue<?> toNMS();

}
