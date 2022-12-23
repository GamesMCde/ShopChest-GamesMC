package de.epiceric.shopchest.transaction;

import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.config.Placeholder;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.Message;
import de.epiceric.shopchest.language.Replacement;

public class SellInformer implements TransactionInformer {

    private Actor buyer, seller;

    @Override
    public void sendNotEnoughMoney() {
        seller.sendMessage(() -> LanguageUtils.getMessage(Message.VENDOR_NOT_ENOUGH_MONEY));
    }

    @Override
    public void sendNotEnoughItem() {
        seller.sendMessage(() -> LanguageUtils.getMessage(Message.NOT_ENOUGH_ITEMS));
    }

    @Override
    public void sendNotEnoughSpace() {
        seller.sendMessage(() -> LanguageUtils.getMessage(Message.CHEST_NOT_ENOUGH_INVENTORY_SPACE));
    }

    @Override
    public void sendInitiatorSuccess(String amount, String productName, double moneyAmountGiven, double moneyAmountRequired) {
        if (buyer.isRepresented()) {
            seller.sendMessage(() -> LanguageUtils.getMessage(Message.SELL_SUCCESS,
                    new Replacement(Placeholder.AMOUNT, amount),
                    new Replacement(Placeholder.ITEM_NAME, productName),
                    new Replacement(Placeholder.SELL_PRICE, String.valueOf(moneyAmountGiven)),
                    new Replacement(Placeholder.VENDOR, buyer.getName())
            ));
            return;
        }

        // IF ADMIN SHOP
        seller.sendMessage(() -> LanguageUtils.getMessage(Message.SELL_SUCCESS_ADMIN,
                new Replacement(Placeholder.AMOUNT, amount),
                new Replacement(Placeholder.ITEM_NAME, productName),
                new Replacement(Placeholder.SELL_PRICE, String.valueOf(moneyAmountGiven))
        ));
    }

    @Override
    public void sendTargetSuccess(String amount, String productName, double moneyAmountGiven, double moneyAmountRequired) {
        if (buyerPlayer.isOnline()) {
            if (Config.enableVendorMessages) {
                shop.getVendor().getPlayer().sendMessage(LanguageUtils.getMessage(Message.SOMEONE_SOLD,
                        new Replacement(Placeholder.AMOUNT, String.valueOf(finalNewAmount)),
                        new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()),
                        new Replacement(Placeholder.SELL_PRICE, String.valueOf(newPrice)),
                        new Replacement(Placeholder.PLAYER, executor.getName())
                ));
            }
        } else if (Config.enableVendorBungeeMessages) {
            String message = LanguageUtils.getMessage(Message.SOMEONE_SOLD,
                    new Replacement(Placeholder.AMOUNT, String.valueOf(finalNewAmount)),
                    new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()),
                    new Replacement(Placeholder.SELL_PRICE, String.valueOf(newPrice)),
                    new Replacement(Placeholder.PLAYER, executor.getName())
            );
            sendBungeeMessage(shop.getVendor().getName(), message);
        }
    }

}
