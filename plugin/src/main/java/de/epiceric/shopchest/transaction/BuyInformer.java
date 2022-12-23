package de.epiceric.shopchest.transaction;

import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.config.Placeholder;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.Message;
import de.epiceric.shopchest.language.Replacement;

public class BuyInformer implements TransactionInformer {

    private Actor buyer, seller;

    @Override
    public void sendNotEnoughMoney() {
        buyer.sendMessage(() -> LanguageUtils.getMessage(Message.NOT_ENOUGH_MONEY));
    }

    @Override
    public void sendNotEnoughItem() {
        buyer.sendMessage(() -> LanguageUtils.getMessage(Message.OUT_OF_STOCK));
    }

    @Override
    public void sendNotEnoughSpace() {
        buyer.sendMessage(() -> LanguageUtils.getMessage(Message.NOT_ENOUGH_INVENTORY_SPACE));
    }

    @Override
    public void sendInitiatorSuccess(String amount, String productName, double moneyAmountGiven, double moneyAmountRequired) {
        if (seller.isRepresented()) {
            buyer.sendMessage(() -> LanguageUtils.getMessage(Message.BUY_SUCCESS,
                    new Replacement(Placeholder.AMOUNT, amount),
                    new Replacement(Placeholder.ITEM_NAME, productName),
                    new Replacement(Placeholder.BUY_PRICE, String.valueOf(moneyAmountRequired)),
                    new Replacement(Placeholder.VENDOR, seller.getName())
            ));
            return;
        }

        buyer.sendMessage(() -> LanguageUtils.getMessage(Message.BUY_SUCCESS_ADMIN,
                new Replacement(Placeholder.AMOUNT, amount),
                new Replacement(Placeholder.ITEM_NAME, productName),
                new Replacement(Placeholder.BUY_PRICE, String.valueOf(moneyAmountRequired))
        ));
    }

    @Override
    public void sendTargetSuccess(String amount, String productName, double moneyAmountGiven, double moneyAmountRequired) {
        if (sellerPlayer.isOnline()) {
            if (Config.enableVendorMessages) {
                shop.getVendor().getPlayer().sendMessage(LanguageUtils.getMessage(Message.SOMEONE_BOUGHT,
                        new Replacement(Placeholder.AMOUNT, String.valueOf(finalNewAmount)),
                        new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()),
                        new Replacement(Placeholder.BUY_PRICE, String.valueOf(newPrice)),
                        new Replacement(Placeholder.PLAYER, executor.getName())
                ));
            }
        } else if (Config.enableVendorBungeeMessages) {
            String message = LanguageUtils.getMessage(Message.SOMEONE_BOUGHT,
                    new Replacement(Placeholder.AMOUNT, String.valueOf(finalNewAmount)),
                    new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()),
                    new Replacement(Placeholder.BUY_PRICE, String.valueOf(newPrice)),
                    new Replacement(Placeholder.PLAYER, executor.getName())
            );
            sendBungeeMessage(shop.getVendor().getName(), message);
        }
    }

}
