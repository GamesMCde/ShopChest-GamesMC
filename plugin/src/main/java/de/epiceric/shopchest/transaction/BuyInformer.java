package de.epiceric.shopchest.transaction;

import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.language.Message;

public class BuyInformer implements TransactionInformer {

    private Actor buyer, seller;

    @Override
    public void sendNotEnoughMoney() {
        buyer.sendMessage(LanguageUtils.getMessage(Message.NOT_ENOUGH_MONEY));
    }

    @Override
    public void sendNotEnoughItem() {
        buyer.sendMessage(LanguageUtils.getMessage(Message.OUT_OF_STOCK));
    }

    @Override
    public void sendNotEnoughSpace() {
        buyer.sendMessage(LanguageUtils.getMessage(Message.NOT_ENOUGH_INVENTORY_SPACE));
    }

}
