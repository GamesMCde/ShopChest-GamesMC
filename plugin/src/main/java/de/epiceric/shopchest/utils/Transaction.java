package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.event.ShopBuySellEvent;

public class Transaction {

    final long timestamp;
    final String productName;
    final int amount;
    final double price;
    final ShopBuySellEvent.Type type;
    final int shopId;

    public Transaction(long timestamp, String productName, int amount, double price, ShopBuySellEvent.Type type, int shopId) {
        this.timestamp = timestamp;
        this.productName = productName;
        this.amount = amount;
        this.price = price;
        this.type = type;
        this.shopId = shopId;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public String getProductName() {
        return productName;
    }
    public int getAmount() {
        return amount;
    }
    public double getPrice() {
        return price;
    }
    public ShopBuySellEvent.Type getType() {
        return type;
    }
    public int getShopId() {
        return shopId;
    }
}
