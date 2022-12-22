package de.epiceric.shopchest.transaction;

public interface TransactionInformer {

    void sendNotEnoughMoney();

    void sendNotEnoughItem();

    void sendNotEnoughSpace();

}
