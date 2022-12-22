package de.epiceric.shopchest.transaction;

import org.bukkit.inventory.ItemStack;

public interface Actor {

    /**
     * Check if this {@link Actor} have a specified amount of money
     *
     * @param moneyAmount The money amount required
     * @return Whether the {@link Actor} have the money
     */
    boolean hasMoney(double moneyAmount);

    /**
     * Check if this {@link Actor} have a specified amount of an item
     *
     * @param itemStack The {@link ItemStack} that represent the product
     * @param amount    The amount to check
     * @return Whether the {@link Actor} have the amount of this item stack or more
     */
    boolean hasProductAmount(ItemStack itemStack, int amount);

    /**
     * Check if this {@link Actor} have enough inventory space to accept this item stack with this amount
     *
     * @param itemStack The {@link ItemStack} that represent the product
     * @param amount    The amount to check
     * @return Whether the {@link Actor} have the space to accept this amount of this item stack in its inventory
     */
    boolean hasEnoughInventorySpace(ItemStack itemStack, int amount);

    /**
     * Send a message to this {@link Actor}
     *
     * @param message The message to send
     */
    void sendMessage(String message);
}
