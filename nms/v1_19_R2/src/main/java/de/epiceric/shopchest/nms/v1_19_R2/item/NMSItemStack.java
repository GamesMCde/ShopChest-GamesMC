package de.epiceric.shopchest.nms.v1_19_R2.item;

import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class NMSItemStack {

    private final ItemStack bukkitItemStack;
    private final net.minecraft.world.item.ItemStack nmsItemStack;

    public NMSItemStack(ItemStack bukkitItemStack) {
        this.bukkitItemStack = bukkitItemStack;
        nmsItemStack = CraftItemStack.asNMSCopy(bukkitItemStack);
    }

    public net.minecraft.world.item.ItemStack getNmsItemStack() {
        return nmsItemStack;
    }

}
