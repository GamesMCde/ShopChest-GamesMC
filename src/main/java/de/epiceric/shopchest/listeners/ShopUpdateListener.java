package de.epiceric.shopchest.listeners;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.event.ShopUpdateEvent;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.ShopUpdater;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ShopUpdateListener implements Listener {

    private ShopChest plugin;

    public ShopUpdateListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShopUpdate(ShopUpdateEvent e) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getShopUtils().updateShops(p);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        restartShopUpdater(e.getPlayer());
    }

    // The Bukkit::getOnlinePlayers() list does not include players that
    // are currently respawning or chaning worlds, so when only one player is
    // online and is currently respawning, the updater will think that no player
    // is online, so it will stop. To prevent that, a delay of 1 tick is needed.

    @EventHandler
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                restartShopUpdater(e.getPlayer());
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                restartShopUpdater(e.getPlayer());
            }
        }.runTaskLater(plugin, 1L);
    }

    private void restartShopUpdater(Player p) {
        if (!plugin.getUpdater().isRunning()) {
            plugin.setUpdater(new ShopUpdater(plugin));
            plugin.getUpdater().start();
        }

        for (Shop shop : plugin.getShopUtils().getShops()) {
            if (shop.getHologram() != null) shop.getHologram().hidePlayer(p);
            if (shop.getItem() != null) shop.getItem().setVisible(p, false);
        }
    }

}
