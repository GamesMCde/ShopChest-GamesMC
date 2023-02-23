package de.epiceric.shopchest.nms.v1_19_R2.component;

import de.epiceric.shopchest.nms.v1_19_R2.item.NMSItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class ItemTagExtractor {

    public String extractItemTag(NMSItemStack itemStack) {
        final Tag tag = itemStack.getNmsItemStack().save(new CompoundTag()).get("tag");
        return tag == null ? null : tag.getAsString();
    }

}
