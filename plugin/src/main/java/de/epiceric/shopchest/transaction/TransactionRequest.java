package de.epiceric.shopchest.transaction;

import de.epiceric.shopchest.shop.Product;

public class TransactionRequest {

    private final Actor buyer, seller;
    private final Product product;
    private final int amount;

    public Transaction prepare() {
        // Calculate price

        // Apply taxes

    }

}
