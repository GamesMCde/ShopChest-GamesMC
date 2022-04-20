package de.epiceric.shopchest.hook;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public interface ExtendShopHook {

    /**
     * Whether the player can extend the shop at a specific block
     *
     * @param newestBlock       The block that is trying to be placed
     * @param currentShopBlocks The current shop's blocks
     * @param player            The player that is trying to place the block
     * @return {@code true} if the player can extend the shop. {@code false} otherwise
     */
    boolean canExtend(Block newestBlock, List<Block> currentShopBlocks, Player player);

}
