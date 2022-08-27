package de.epiceric.shopchest.shop;

import org.bukkit.inventory.ItemStack;

public class Shop_ {

    private final ItemStack itemStack;
    private final ProductValue buyValue, sellValue;

    public Product getBuyProduct() {
        return new Product(itemStack, buyValue);
    }

    public Product getSellProduct() {
        return new Product(itemStack, sellValue);
    }

}
