package de.epiceric.shopchest.nms.v1_18_R2.metadata;

import de.epiceric.shopchest.nms.metadata.MetadataValue;
import net.minecraft.network.syncher.SynchedEntityData;

public interface ExplicitMetadataValue extends MetadataValue {

    SynchedEntityData.DataItem<?> toNMS();

}
