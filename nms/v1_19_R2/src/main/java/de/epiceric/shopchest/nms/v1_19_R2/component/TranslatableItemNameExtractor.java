package de.epiceric.shopchest.nms.v1_19_R2.component;

import de.epiceric.shopchest.nms.v1_19_R2.item.NMSItemStack;
import net.minecraft.network.chat.Component;

public class TranslatableItemNameExtractor {

    public String extractTranslatableItemName(NMSItemStack nmsItemStack) {
        final net.minecraft.world.item.ItemStack nStack = nmsItemStack.getNmsItemStack();
        final Component component = nStack.getItem().getName(nStack);
        return Component.Serializer.toJson(component);
    }

}
