package de.epiceric.shopchest.listeners;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.utils.Callback;

public class ShopUpdateListener implements Listener {

    private final ShopChest plugin;
    private final Set<Chunk> newLoadedChunks = new HashSet<>();

    public ShopUpdateListener(ShopChest plugin) {
        this.plugin = plugin;
    }

    // Update hologram replacement if items move
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryUpdate(InventoryMoveItemEvent e) {
        if (!plugin.getHologramFormat().isDynamic()) return;

        Location loc = null;

        if (e.getSource().getHolder() instanceof Chest || e.getSource().getHolder() instanceof ShulkerBox || e.getSource().getHolder() instanceof Barrel) {
            loc =  ((BlockState) e.getSource().getHolder()).getLocation();
        } else if (e.getSource().getHolder() instanceof DoubleChest) {
            loc =  ((DoubleChest) e.getSource().getHolder()).getLocation();
        } else if (e.getDestination().getHolder() instanceof Chest || e.getDestination().getHolder() instanceof ShulkerBox || e.getDestination().getHolder() instanceof Barrel) {
            loc =  ((BlockState) e.getDestination().getHolder()).getLocation();
        } else if (e.getDestination().getHolder() instanceof DoubleChest) {
            loc =  ((DoubleChest) e.getDestination().getHolder()).getLocation();
        }

        if (loc != null) {
            Shop shop = plugin.getShopUtils().getShop(loc);
            if (shop != null) shop.updateHologramText();
        }
    }

    // Remove the player tracking when leaving the server
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        // If done without delay, Bukkit#getOnlinePlayers() would still
        // contain the player even though he left, so the shop updater
        // would show the shop again.
        new BukkitRunnable(){
            @Override
            public void run() {
                for (Shop shop : plugin.getShopUtils().getShops()) {
                    if (shop.hasItem()) {
                        shop.getItem().resetVisible(e.getPlayer());
                    }
                    if (shop.hasHologram()) {
                        shop.getHologram().resetVisible(e.getPlayer());
                    }
                }
        
                plugin.getShopUtils().resetPlayerLocation(e.getPlayer());
            }
        }.runTaskLater(plugin, 1L);
    }

    // Update player tracker when teleport
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();
        final Player p = e.getPlayer();

        // Wait till the chunk should have loaded on the client
        if (!from.getWorld().getName().equals(to.getWorld().getName())
                || from.getChunk().getX() != to.getChunk().getX()
                || from.getChunk().getZ() != to.getChunk().getZ()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getUpdater().queue(() -> {
                        if (p.isOnline()) {
                            for (Shop shop : plugin.getShopUtils().getShops()) {
                                if (shop.hasItem()) {
                                    shop.getItem().hidePlayer(p);
                                }
                                if (shop.hasHologram()) {
                                    shop.getHologram().hidePlayer(p);
                                }
                            }
                            plugin.getShopUtils().resetPlayerLocation(p);
                        }
                    });
                    plugin.getUpdater().updateShops(p);
                }
            }.runTaskLater(plugin, 15L);
        }
    }

    // Update the shop while moving
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        plugin.getUpdater().updateShops(e.getPlayer());
    }

    // Load shop when chunk are loading
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (!plugin.getShopDatabase().isInitialized()) {
            return;
        }

        // Wait 10 ticks after first event is triggered, so that multiple
        // chunk loads can be handled at the same time without having to
        // send a database request for each chunk.
        if (newLoadedChunks.isEmpty()) {
            new BukkitRunnable(){
                @Override
                public void run() {
                    int chunkCount = newLoadedChunks.size();
                    plugin.getShopUtils().loadShops(newLoadedChunks.toArray(new Chunk[chunkCount]), new Callback<Integer>(plugin) {
                        @Override
                        public void onResult(Integer result) {
                            if (result == 0) {
                                return;
                            }
                            plugin.getDebugLogger().debug("Loaded " + result + " shops in " + chunkCount + " chunks");
                        }
            
                        @Override
                        public void onError(Throwable throwable) {
                            // Database connection probably failed => disable plugin to prevent more errors
                            plugin.getLogger().severe("Failed to load shops in newly loaded chunks");
                            plugin.getDebugLogger().debug("Failed to load shops in newly loaded chunks");
                            if (throwable != null) plugin.getDebugLogger().debug(throwable);
                        }
                    });
                    newLoadedChunks.clear();
                }
            }.runTaskLater(plugin, 10L);
        }

        newLoadedChunks.add(e.getChunk());
    }
}
