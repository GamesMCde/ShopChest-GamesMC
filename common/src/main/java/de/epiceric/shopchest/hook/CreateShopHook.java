package de.epiceric.shopchest.hook;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public interface CreateShopHook {

    /**
     * Whether the player can create a shop at a specific block
     *
     * @param clickedBlock The {@link Block} that player target
     * @param shopBlocks   The {@link Block} that represents this shop
     * @param player       The player that is trying to create the shop
     * @return {@code true} if the player can create the shop. {@code false} otherwise
     */
    boolean canCreate(Block clickedBlock, List<Block> shopBlocks, Player player);

}
