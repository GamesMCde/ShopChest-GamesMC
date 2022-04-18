package de.epiceric.shopchest.hook;

import org.bukkit.entity.Player;

public interface InteractHook {

    /**
     * <p>Whether the player can interact with shops on the server</p>
     * <p>Called by the plugin when a player try to interact with shops</p>
     *
     * @param player The {@link Player} that try to interact
     * @return {@code true} if the player can interact with shops. {@code false} otherwise
     */
    boolean canInteract(Player player);

}
