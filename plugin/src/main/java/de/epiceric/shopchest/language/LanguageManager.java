package de.epiceric.shopchest.language;

import org.jetbrains.annotations.NotNull;

import de.epiceric.shopchest.language.item.ItemNameManager;

public class LanguageManager {

    private final MessageRegistry messageRegistry;
    private final ItemNameManager itemNameManager;

    public LanguageManager(@NotNull MessageRegistry messageRegistry, @NotNull ItemNameManager localizedItemManager) {
        this.messageRegistry = messageRegistry;
        this.itemNameManager = localizedItemManager;
    }

    @NotNull
    public MessageRegistry getMessageRegistry() {
        return messageRegistry;
    }

    @NotNull
    public ItemNameManager getItemNameManager() {
        return itemNameManager;
    }

}
