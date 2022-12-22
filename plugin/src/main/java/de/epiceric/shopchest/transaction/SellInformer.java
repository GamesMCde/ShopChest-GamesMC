package de.epiceric.shopchest.transaction;

import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.Message;

public class SellInformer implements TransactionInformer {

    private Actor buyer, seller;

    @Override
    public void sendNotEnoughMoney() {
        seller.sendMessage(LanguageUtils.getMessage(Message.VENDOR_NOT_ENOUGH_MONEY));
    }

    @Override
    public void sendNotEnoughItem() {
        seller.sendMessage(LanguageUtils.getMessage(Message.NOT_ENOUGH_ITEMS));
    }

    @Override
    public void sendNotEnoughSpace() {
        seller.sendMessage(LanguageUtils.getMessage(Message.CHEST_NOT_ENOUGH_INVENTORY_SPACE));
    }

}
