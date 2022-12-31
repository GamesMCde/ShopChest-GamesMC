package de.epiceric.shopchest.nms.v1_19_R2.metadata;

import de.epiceric.shopchest.nms.metadata.MetadataValue;
import net.minecraft.network.syncher.SynchedEntityData;

public interface ExplicitMetadataValue extends MetadataValue {

    SynchedEntityData.DataValue<?> toNMS();

}
