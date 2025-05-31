package de.epiceric.shopchest.config;

public enum Placeholder {

    VENDOR("%VENDOR%"),
    AMOUNT("%AMOUNT%"),
    ITEM_NAME("%ITEMNAME%"),
    CREATION_PRICE("%CREATION-PRICE%"),
    MODIFY_PRICE("%MODIFY-PRICE%"),
    ERROR("%ERROR%"),
    ENCHANTMENT("%ENCHANTMENT%"),
    MIN_PRICE("%MIN-PRICE%"),
    MAX_PRICE("%MAX-PRICE%"),
    VERSION("%VERSION%"),
    BUY_PRICE("%BUY-PRICE%"),
    SELL_PRICE("%SELL-PRICE%"),
    LIMIT("%LIMIT%"),
    PLAYER("%PLAYER%"),
    POTION_EFFECT("%POTION-EFFECT%"),
    MUSIC_TITLE("%MUSIC-TITLE%"),
    BANNER_PATTERN_NAME("%BANNER-PATTERN-NAME%"),
    PROPERTY("%PROPERTY%"),
    VALUE("%VALUE%"),
    EXTENDED("%EXTENDED%"),
    REVENUE("%REVENUE%"),
    GENERATION("%GENERATION%"),
    STOCK("%STOCK%"),
    CHEST_SPACE("%CHEST-SPACE%"),
    MAX_STACK("%MAX-STACK%"),
    COMMAND("%COMMAND%"),
    DURABILITY("%DURABILITY%"),
    PAGE("%PAGE%"),
    TOTAL_PAGES("%TOTAL-PAGES%"),
    TOTAL_SHOPS("%TOTAL-SHOPS%"),
    LOCATION("%LOCATION%"),
    PRICE("%PRICE%"),
    TIME("%TIME%"),
    SUM_BUY("%SUM-BUY%"),
    SUM_SELL("%SUM-SELL%"),
    SUM_TRANSACTIONS("%SUM-TRANSACTIONS%");

    private final String name;

    Placeholder(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

}
