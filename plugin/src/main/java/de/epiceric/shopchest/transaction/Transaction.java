package de.epiceric.shopchest.transaction;

import org.bukkit.inventory.ItemStack;

public class Transaction {

    private final Actor buyer, seller;
    private final ItemStack itemStack;
    private int amount;
    private final double buyPrice, sellPrice;

    public void apply() {
        if(!check()) {
            return;
        }

        transferItems();

        transferMoney();

        inform();

        // log
    }

    private boolean check() {
        // Check buyer money

        // Check seller item quantity

        // Check buyer inventory space
    }

    private void transferItems() {
        // Remove items from seller inventory

        // Add items in buyer inventory
    }

    private void transferMoney() {
        // Remove buyer money

        // Add seller money
    }

    private void inform() {
        // Inform buyer

        // Inform seller
    }

}
