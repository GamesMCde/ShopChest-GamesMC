package de.epiceric.shopchest.transaction;

import de.epiceric.shopchest.event.ShopBuySellEvent;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.Message;
import org.bukkit.inventory.ItemStack;

public class Transaction {

    // A direct transaction is initiated by the buyer
    // A indirect transaction is initiated by the seller
    private final boolean directTransaction;
    private final Actor buyer, seller;
    private final ItemStack itemStack;
    private int amount;
    private final double moneyAmountRequired, moneyAmountGiven;

    public void apply() {
        if (!check()) {
            return;
        }

        // Call buy or sell event
        if (processEvent()) {
            return;
        }

        transferItems();

        transferMoney();

        inform();

        // log
    }

    /**
     * Check if the transaction can be performed and inform actors
     *
     * @return {@code true} if the transaction can be performed
     */
    private boolean check() {
        // Check buyer money
        if (!buyer.hasMoney(moneyAmountRequired)) {
            if (directTransaction) {
                buyer.sendMessage(LanguageUtils.getMessage(Message.NOT_ENOUGH_MONEY));
            } else {
                seller.sendMessage(LanguageUtils.getMessage(Message.VENDOR_NOT_ENOUGH_MONEY));
            }
            return false;
        }

        // Check seller item quantity
        if (!seller.hasProductAmount(itemStack, amount)) {
            if (directTransaction) {
                buyer.sendMessage(LanguageUtils.getMessage(Message.OUT_OF_STOCK));
            } else {
                seller.sendMessage(LanguageUtils.getMessage(Message.NOT_ENOUGH_ITEMS));
            }
            return false;
        }

        // Check buyer inventory space
        if (!buyer.hasEnoughInventorySpace(itemStack, amount)) {
            if (directTransaction) {
                buyer.sendMessage(LanguageUtils.getMessage(Message.NOT_ENOUGH_INVENTORY_SPACE));
            } else {
                seller.sendMessage(LanguageUtils.getMessage(Message.CHEST_NOT_ENOUGH_INVENTORY_SPACE));
            }
            return false;
        }
        return true;
    }

    /**
     * Call {@link ShopBuySellEvent}
     *
     * @return {@code true} if the event is cancelled. {@code false} otherwise.
     */
    private boolean processEvent() {
        /*
        final ShopBuySellEvent transactionEvent = new ShopBuySellEvent();
        Bukkit.getPluginManager().callEvent(transactionEvent);
        return transactionEvent.isCancelled();
        */
        return true;
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
