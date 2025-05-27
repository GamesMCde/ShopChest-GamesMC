package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.event.ShopBuySellEvent;
import de.epiceric.shopchest.shop.Shop;

import java.util.Collection;

public class ShopReport {
    private final Shop shop;
    private final Collection<Transaction> transactions;

    private final double sumBuy;
    private final double sumSell;

    public ShopReport(Shop shop, Collection<Transaction> transactions) {
        this.shop = shop;
        this.transactions = transactions;
        this.sumBuy = transactions.stream()
                .filter(transaction -> transaction.getType() == ShopBuySellEvent.Type.BUY)
                .mapToDouble(Transaction::getPrice)
                .sum();
        this.sumSell = transactions.stream()
                .filter(transaction -> transaction.getType() == ShopBuySellEvent.Type.SELL)
                .mapToDouble(Transaction::getPrice)
                .sum();
    }

    public Shop getShop() {
        return shop;
    }

    public double getSumBuy() {
        return sumBuy;
    }

    public double getSumSell() {
        return sumSell;
    }

    public Collection<Transaction> getTransactions() {
        return transactions;
    }
}
