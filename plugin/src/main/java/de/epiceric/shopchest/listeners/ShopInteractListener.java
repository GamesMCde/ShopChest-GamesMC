package de.epiceric.shopchest.listeners;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.config.Placeholder;
import de.epiceric.shopchest.event.*;
import de.epiceric.shopchest.external.PlotSquaredOldShopFlag;
import de.epiceric.shopchest.external.PlotSquaredShopFlag;
import de.epiceric.shopchest.language.Message;
import de.epiceric.shopchest.language.MessageRegistry;
import de.epiceric.shopchest.language.Replacement;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.shop.ShopProduct;
import de.epiceric.shopchest.sql.Database;
import de.epiceric.shopchest.utils.*;
import de.epiceric.shopchest.utils.ClickType.CreateClickType;
import fr.xephi.authme.api.v3.AuthMeApi;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.*;
import java.util.function.Consumer;

public class ShopInteractListener implements Listener {

    private ShopChest plugin;
    private Economy econ;
    private Database database;
    private ShopUtils shopUtils;

    public ShopInteractListener(ShopChest plugin) {
        this.plugin = plugin;
        this.econ = plugin.getEconomy();
        this.database = plugin.getShopDatabase();
        this.shopUtils = plugin.getShopUtils();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!plugin.getHologramFormat().isDynamic()) return;

        Inventory chestInv = e.getInventory();

        if (!(chestInv.getHolder() instanceof Chest || chestInv.getHolder() instanceof DoubleChest || chestInv.getHolder() instanceof Barrel || chestInv.getHolder() instanceof ShulkerBox)) {
            return;
        }

        Location loc = null;
        if (chestInv.getHolder() instanceof DoubleChest)
        {
            loc = ((DoubleChest) chestInv.getHolder()).getLocation();
        }
        else
        {
            loc = ((Container) chestInv.getHolder()).getLocation();
        }

        final Shop shop = plugin.getShopUtils().getShop(loc);
        if (shop == null) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                shop.updateHologramText();
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractCreate(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block b = e.getClickedBlock();

        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if(ClickType.getPlayerClickType(p) instanceof ClickType.ModifyClickType){
            if (!shopUtils.isShop(b.getLocation())) {
                p.sendMessage(messageRegistry.getMessage(Message.CHEST_ALREADY_SHOP));
                plugin.debug("Chest is not a shop");
            }else
            {
                Shop shop = shopUtils.getShop(b.getLocation());
                if(!p.getUniqueId().equals(shop.getVendor().getUniqueId()))
                {
                    p.sendMessage(messageRegistry.getMessage(Message.NO_PERMISSION_MODIFY));
                    plugin.debug(p.getName() + " is not allowed to modify the shop");
                } else {
                    performModify(p, (ClickType.ModifyClickType) ClickType.getPlayerClickType(p), shop);
                }
            }
            e.setCancelled(true);
            ClickType.removePlayerClickType(p);
            return;
        }

        if (!(ClickType.getPlayerClickType(p) instanceof CreateClickType))
            return;

        if (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST && b.getType() != Material.BARREL && b.getType() != Material.SHULKER_BOX)
            return;

        if (ClickType.getPlayerClickType(p).getClickType() != ClickType.EnumClickType.CREATE)
            return;

        if (Config.enableAuthMeIntegration && plugin.hasAuthMe() && !AuthMeApi.getInstance().isAuthenticated(p))
            return;

        if (e.isCancelled() && !p.hasPermission(Permissions.CREATE_PROTECTED)) {
            p.sendMessage(messageRegistry.getMessage(Message.NO_PERMISSION_CREATE_PROTECTED));
            plugin.debug(p.getName() + " is not allowed to create a shop on the selected chest");
        } else if (shopUtils.isShop(b.getLocation())) {
            p.sendMessage(messageRegistry.getMessage(Message.CHEST_ALREADY_SHOP));
            plugin.debug("Chest is already a shop");
        } else if (!ItemUtils.isAir(b.getRelative(BlockFace.UP).getType())) {
            p.sendMessage(messageRegistry.getMessage(Message.CHEST_BLOCKED));
            plugin.debug("Chest is blocked");
        } else {
            CreateClickType clickType = (CreateClickType) ClickType.getPlayerClickType(p);
            ShopProduct product = clickType.getProduct();
            double buyPrice = clickType.getBuyPrice();
            double sellPrice = clickType.getSellPrice();
            ShopType shopType = clickType.getShopType();
    
            create(p, b.getLocation(), product, buyPrice, sellPrice, shopType);
        }

        e.setCancelled(true);
        ClickType.removePlayerClickType(p);
    }

    private Map<UUID, Set<Integer>> needsConfirmation = new HashMap<>();

    private void handleInteractEvent(PlayerInteractEvent e) {
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

        Block b = e.getClickedBlock();
        Player p = e.getPlayer();
        boolean inverted = Config.invertMouseButtons;

        if (Utils.getMajorVersion() >= 9 && e.getHand() == EquipmentSlot.OFF_HAND)
            return;

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_BLOCK)
            return;
        
        if (b.getType() != Material.CHEST && b.getType() != Material.TRAPPED_CHEST && b.getType() != Material.BARREL && b.getType() != Material.SHULKER_BOX)
            return;
        
        ClickType clickType = ClickType.getPlayerClickType(p);
        if (clickType != null) {
            if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
                return;

            Shop shop = shopUtils.getShop(b.getLocation());
            switch (clickType.getClickType()) {
                case CREATE:
                case SELECT_ITEM:
                case MODIFY:
                    break;
                default: 
                    if (shop == null) {
                        p.sendMessage(messageRegistry.getMessage(Message.CHEST_NO_SHOP));
                        plugin.debug("Chest is not a shop");
                        return;
                    }
            }

            switch (clickType.getClickType()) {
                case INFO:
                    info(p, shop);
                    break;
                case REMOVE:
                    remove(p, shop);
                    break;
                case OPEN:
                    open(p, shop, true);
                    break;
                default: return;
            }

            e.setCancelled(true);
            ClickType.removePlayerClickType(p);
        } else {
            Shop shop = shopUtils.getShop(b.getLocation());

            if (shop == null)
                return;

            boolean confirmed = needsConfirmation.containsKey(p.getUniqueId()) && needsConfirmation.get(p.getUniqueId()).contains(shop.getID());
            
            if (e.getAction() == Action.LEFT_CLICK_BLOCK && p.isSneaking() && Utils.hasAxeInHand(p)) {
                return;
            }

            ItemStack infoItem = Config.shopInfoItem;
            if (infoItem != null) {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    ItemStack item = Utils.getItemInMainHand(p);

                    if (item == null || !(infoItem.getType() == item.getType() && infoItem.getDurability() == item.getDurability())) {
                        item = Utils.getItemInOffHand(p);

                        if (item != null && infoItem.getType() == item.getType() && infoItem.getDurability() == item.getDurability()) {
                            e.setCancelled(true);
                            info(p, shop);
                            return;
                        }
                    } else {
                        e.setCancelled(true);
                        info(p, shop);
                        return;
                    }
                }
            }

            if (e.getAction() == Action.RIGHT_CLICK_BLOCK && p.getUniqueId().equals(shop.getVendor().getUniqueId()) && shop.getShopType() != ShopType.ADMIN) {
                return;
            }

            if (p.getGameMode() == GameMode.CREATIVE) {
                e.setCancelled(true);
                p.sendMessage(messageRegistry.getMessage(Message.USE_IN_CREATIVE));
                return;
            }

            if ((e.getAction() == Action.RIGHT_CLICK_BLOCK && !inverted) || (e.getAction() == Action.LEFT_CLICK_BLOCK && inverted)) {
                e.setCancelled(true);

                if (shop.getShopType() == ShopType.ADMIN || !shop.getVendor().getUniqueId().equals(p.getUniqueId())) {
                    plugin.debug(p.getName() + " wants to buy");

                    if (shop.getBuyPrice() > 0) {
                        if (p.hasPermission(Permissions.BUY)) {
                            // TODO: Outsource shop use external permission
                            boolean externalPluginsAllowed = true;

                            if (plugin.hasPlotSquared() && Config.enablePlotsquaredIntegration) {
                                try {
                                    Class.forName("com.plotsquared.core.PlotSquared");
                                    com.plotsquared.core.location.Location plotLocation =
                                            com.plotsquared.core.location.Location.at(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());
                                    com.plotsquared.core.plot.Plot plot = plotLocation.getOwnedPlot();
                                    externalPluginsAllowed = PlotSquaredShopFlag.isFlagAllowedOnPlot(plot, PlotSquaredShopFlag.USE_SHOP, p);
                                } catch (ClassNotFoundException ex) {
                                    com.github.intellectualsites.plotsquared.plot.object.Location plotLocation =
                                            new com.github.intellectualsites.plotsquared.plot.object.Location(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());
                                    com.github.intellectualsites.plotsquared.plot.object.Plot plot = plotLocation.getOwnedPlot();
                                    externalPluginsAllowed = PlotSquaredOldShopFlag.isFlagAllowedOnPlot(plot, PlotSquaredOldShopFlag.USE_SHOP, p);
                                }
                            }

                            if (externalPluginsAllowed && plugin.hasWorldGuard() && Config.enableWorldGuardIntegration) {
                                String flagName = (shop.getShopType() == ShopType.ADMIN ? "use-admin-shop" : "use-shop");
                                WorldGuardWrapper wgWrapper = WorldGuardWrapper.getInstance();
                                Optional<IWrappedFlag<WrappedState>> flag = wgWrapper.getFlag(flagName, WrappedState.class);
                                if (!flag.isPresent()) plugin.debug("WorldGuard flag '" + flagName + "' is not present!");
                                WrappedState state = flag.map(f -> wgWrapper.queryFlag(p, b.getLocation(), f).orElse(WrappedState.DENY)).orElse(WrappedState.DENY);
                                externalPluginsAllowed = state == WrappedState.ALLOW;
                            }
                            
                            if (shop.getShopType() == ShopType.ADMIN) {
                                if (externalPluginsAllowed || p.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGIN)) {
                                    if (confirmed || !Config.confirmShopping || getBuyPrice(shop, p.isSneaking())<Config.confirmShoppingThreshold) {
                                        buy(p, shop, p.isSneaking());
                                        if (Config.confirmShopping) {
                                            Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                            ids.remove(shop.getID());
                                            if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                            else needsConfirmation.put(p.getUniqueId(), ids);
                                        }
                                    } else {
                                        plugin.debug("Needs confirmation");
                                        p.sendMessage(messageRegistry.getMessage(Message.CLICK_TO_CONFIRM));
                                        Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                        ids.add(shop.getID());
                                        needsConfirmation.put(p.getUniqueId(), ids);
                                    }
                                } else {
                                    plugin.debug(p.getName() + " doesn't have external plugin's permission");
                                    p.sendMessage(messageRegistry.getMessage(Message.NO_PERMISSION_BUY_HERE));
                                }
                            } else {
                                if (externalPluginsAllowed || p.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGIN)) {
                                    Container c = (Container) b.getState();
                                    ItemStack itemStack = shop.getProduct().getItemStack();
                                    int amount = (p.isSneaking() ? itemStack.getMaxStackSize() : shop.getProduct().getAmount());

                                    // If shop has higher amounts than a stack, use the shop amount to allow players to use bulk discount
                                    if(shop.getProduct().getAmount()>itemStack.getMaxStackSize())
                                        amount = shop.getProduct().getAmount();

                                    if (Utils.getAmount(c.getInventory(), itemStack) >= amount) {
                                        if (confirmed || !Config.confirmShopping || getBuyPrice(shop, p.isSneaking())<Config.confirmShoppingThreshold) {
                                            buy(p, shop, p.isSneaking());
                                            if (Config.confirmShopping) {
                                                Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                ids.remove(shop.getID());
                                                if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                                else needsConfirmation.put(p.getUniqueId(), ids);
                                            }
                                        } else {
                                            plugin.debug("Needs confirmation");
                                            p.sendMessage(messageRegistry.getMessage(Message.CLICK_TO_CONFIRM));
                                            Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                            ids.add(shop.getID());
                                            needsConfirmation.put(p.getUniqueId(), ids);
                                        }
                                    } else {
                                        if (Config.autoCalculateItemAmount && Utils.getAmount(c.getInventory(), itemStack) > 0) {
                                            if (confirmed || !Config.confirmShopping || getBuyPrice(shop, p.isSneaking())<Config.confirmShoppingThreshold) {
                                                buy(p, shop, p.isSneaking());
                                                if (Config.confirmShopping) {
                                                    Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                    ids.remove(shop.getID());
                                                    if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                                    else needsConfirmation.put(p.getUniqueId(), ids);
                                                }
                                            } else {
                                                plugin.debug("Needs confirmation");
                                                p.sendMessage(messageRegistry.getMessage(Message.CLICK_TO_CONFIRM));
                                                Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                ids.add(shop.getID());
                                                needsConfirmation.put(p.getUniqueId(), ids);
                                            }
                                        } else {
                                            p.sendMessage(messageRegistry.getMessage(Message.OUT_OF_STOCK));
                                            if (shop.getVendor().isOnline() && Config.enableVendorMessages) {
                                                shop.getVendor().getPlayer().sendMessage(messageRegistry.getMessage(Message.VENDOR_OUT_OF_STOCK,
                                                        new Replacement(Placeholder.AMOUNT, String.valueOf(shop.getProduct().getAmount())),
                                                        new Replacement(Placeholder.ITEM_NAME, shop.getProduct().getLocalizedName())));
                                            } else if(!shop.getVendor().isOnline() && Config.enableVendorBungeeMessages){
                                                String message = messageRegistry.getMessage(Message.VENDOR_OUT_OF_STOCK,
                                                        new Replacement(Placeholder.AMOUNT, String.valueOf(shop.getProduct().getAmount())),
                                                        new Replacement(Placeholder.ITEM_NAME, shop.getProduct().getLocalizedName()));
                                                sendBungeeMessage(shop.getVendor().getName(), message);
                                            }
                                            plugin.debug("Shop is out of stock");
                                        }
                                    }
                                } else {
                                    plugin.debug(p.getName() + " doesn't have external plugin's permission");
                                    p.sendMessage(messageRegistry.getMessage(Message.NO_PERMISSION_BUY_HERE));
                                }
                            }
                        } else {
                            p.sendMessage(messageRegistry.getMessage(Message.NO_PERMISSION_BUY));
                            plugin.debug(p.getName() + " is not permitted to buy");
                        }
                    } else {
                        p.sendMessage(messageRegistry.getMessage(Message.BUYING_DISABLED));
                        plugin.debug("Buying is disabled");
                    }
                }

            } else if ((e.getAction() == Action.LEFT_CLICK_BLOCK && !inverted) || (e.getAction() == Action.RIGHT_CLICK_BLOCK && inverted)) {
                e.setCancelled(true);

                if ((shop.getShopType() == ShopType.ADMIN) || (!shop.getVendor().getUniqueId().equals(p.getUniqueId()))) {
                    plugin.debug(p.getName() + " wants to sell");

                    if (shop.getSellPrice() > 0) {
                        if (p.hasPermission(Permissions.SELL)) {
                            // TODO: Outsource shop use external permission
                            boolean externalPluginsAllowed = true;

                            if (plugin.hasPlotSquared() && Config.enablePlotsquaredIntegration) {
                                try {
                                    Class.forName("com.plotsquared.core.PlotSquared");
                                    com.plotsquared.core.location.Location plotLocation =
                                            com.plotsquared.core.location.Location.at(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());
                                    com.plotsquared.core.plot.Plot plot = plotLocation.getOwnedPlot();
                                    externalPluginsAllowed = PlotSquaredShopFlag.isFlagAllowedOnPlot(plot, PlotSquaredShopFlag.USE_SHOP, p);
                                } catch (ClassNotFoundException ex) {
                                    com.github.intellectualsites.plotsquared.plot.object.Location plotLocation =
                                            new com.github.intellectualsites.plotsquared.plot.object.Location(b.getWorld().getName(), b.getX(), b.getY(), b.getZ());
                                    com.github.intellectualsites.plotsquared.plot.object.Plot plot = plotLocation.getOwnedPlot();
                                    externalPluginsAllowed = PlotSquaredOldShopFlag.isFlagAllowedOnPlot(plot, PlotSquaredOldShopFlag.USE_SHOP, p);
                                }
                            }

                            if (externalPluginsAllowed && plugin.hasWorldGuard() && Config.enableWorldGuardIntegration) {
                                String flagName = (shop.getShopType() == ShopType.ADMIN ? "use-admin-shop" : "use-shop");
                                WorldGuardWrapper wgWrapper = WorldGuardWrapper.getInstance();
                                Optional<IWrappedFlag<WrappedState>> flag = wgWrapper.getFlag(flagName, WrappedState.class);
                                if (!flag.isPresent()) plugin.debug("WorldGuard flag '" + flagName + "' is not present!");
                                WrappedState state = flag.map(f -> wgWrapper.queryFlag(p, b.getLocation(), f).orElse(WrappedState.DENY)).orElse(WrappedState.DENY);
                                externalPluginsAllowed = state == WrappedState.ALLOW;
                            }

                            ItemStack itemStack = shop.getProduct().getItemStack();

                            if (externalPluginsAllowed || p.hasPermission(Permissions.BYPASS_EXTERNAL_PLUGIN)) {
                                boolean stack = p.isSneaking() && !Utils.hasAxeInHand(p);
                                int amount = (stack && shop.getProduct().getAmount()<= itemStack.getMaxStackSize()) ? itemStack.getMaxStackSize() : shop.getProduct().getAmount();

                                if (Utils.getAmount(p.getInventory(), itemStack) >= amount) {
                                    if (confirmed || !Config.confirmShopping || getSellPrice(shop, p.isSneaking())<Config.confirmShoppingThreshold) {
                                        sell(p, shop, stack);
                                        if (Config.confirmShopping) {
                                            Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                            ids.remove(shop.getID());
                                            if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                            else needsConfirmation.put(p.getUniqueId(), ids);
                                        }
                                    } else {
                                        plugin.debug("Needs confirmation");
                                        p.sendMessage(messageRegistry.getMessage(Message.CLICK_TO_CONFIRM));
                                        Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                        ids.add(shop.getID());
                                        needsConfirmation.put(p.getUniqueId(), ids);
                                    }
                                } else {
                                    if (Config.autoCalculateItemAmount && Utils.getAmount(p.getInventory(), itemStack) > 0) {
                                        if (confirmed || !Config.confirmShopping || getSellPrice(shop, p.isSneaking())<Config.confirmShoppingThreshold) {
                                            sell(p, shop, stack);
                                            if (Config.confirmShopping) {
                                                Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                                ids.remove(shop.getID());
                                                if (ids.isEmpty()) needsConfirmation.remove(p.getUniqueId());
                                                else needsConfirmation.put(p.getUniqueId(), ids);
                                            }
                                        } else {
                                            plugin.debug("Needs confirmation");
                                            p.sendMessage(messageRegistry.getMessage(Message.CLICK_TO_CONFIRM));
                                            Set<Integer> ids = needsConfirmation.containsKey(p.getUniqueId()) ? needsConfirmation.get(p.getUniqueId()) : new HashSet<Integer>();
                                            ids.add(shop.getID());
                                            needsConfirmation.put(p.getUniqueId(), ids);
                                        }
                                    } else {
                                        p.sendMessage(messageRegistry.getMessage(Message.NOT_ENOUGH_ITEMS));
                                        plugin.debug(p.getName() + " doesn't have enough items");
                                    }
                                }
                            } else {
                                plugin.debug(p.getName() + " doesn't have external plugin's permission");
                                p.sendMessage(messageRegistry.getMessage(Message.NO_PERMISSION_SELL_HERE));
                            }
                        } else {
                            p.sendMessage(messageRegistry.getMessage(Message.NO_PERMISSION_SELL));
                            plugin.debug(p.getName() + " is not permitted to sell");
                        }
                    } else {
                        p.sendMessage(messageRegistry.getMessage(Message.SELLING_DISABLED));
                        plugin.debug("Selling is disabled");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (Config.enableAuthMeIntegration && plugin.hasAuthMe() && !AuthMeApi.getInstance().isAuthenticated(e.getPlayer())) return;
        handleInteractEvent(e);
    }

    /**
     * Create a new shop
     *
     * @param executor  Player, who executed the command, will receive the message and become the vendor of the shop
     * @param location  Where the shop will be located
     * @param product   Product of the Shop
     * @param buyPrice  Buy price
     * @param sellPrice Sell price
     * @param shopType  Type of the shop
     */
    private void create(final Player executor, final Location location, final ShopProduct product, final double buyPrice, final double sellPrice, final ShopType shopType) {
        plugin.debug(executor.getName() + " is creating new shop...");

        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

        if (!executor.hasPermission(Permissions.CREATE)) {
            executor.sendMessage(messageRegistry.getMessage(Message.NO_PERMISSION_CREATE));
            plugin.debug(executor.getName() + " is not permitted to create the shop");
            return;
        }

        double creationPrice = (shopType == ShopType.NORMAL) ? Config.shopCreationPriceNormal : Config.shopCreationPriceAdmin;
        Shop shop = new Shop(plugin, executor, product, location, buyPrice, sellPrice, shopType);

        ShopCreateEvent event = new ShopCreateEvent(executor, shop, creationPrice);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() && !executor.hasPermission(Permissions.CREATE_PROTECTED)) {
            plugin.debug("Create event cancelled");
            executor.sendMessage(messageRegistry.getMessage(Message.NO_PERMISSION_CREATE_PROTECTED));
            return;
        }

        if (creationPrice > 0) {
            EconomyResponse r = plugin.getEconomy().withdrawPlayer(executor, location.getWorld().getName(), creationPrice);
            if (!r.transactionSuccess()) {
                plugin.debug("Economy transaction failed: " + r.errorMessage);
                executor.sendMessage(messageRegistry.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, r.errorMessage)));
                return;
            }
        }

        shop.create(true);

        plugin.debug("Shop created");
        shopUtils.addShop(shop, true);

        Message message = shopType == ShopType.ADMIN ? Message.ADMIN_SHOP_CREATED : Message.SHOP_CREATED;
        executor.sendMessage(messageRegistry.getMessage(message, new Replacement(Placeholder.CREATION_PRICE, creationPrice)));
    }

    /**
     * Remove a shop
     * @param executor Player, who executed the command and will receive the message
     * @param shop Shop to be removed
     */
    private void remove(Player executor, Shop shop) {
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

        if (shop.getShopType() == ShopType.ADMIN && !executor.hasPermission(Permissions.REMOVE_ADMIN)) {
            executor.sendMessage(messageRegistry.getMessage(Message.NO_PERMISSION_REMOVE_ADMIN));
            return;
        }

        if (shop.getShopType() == ShopType.NORMAL && !executor.getUniqueId().equals(shop.getVendor().getUniqueId())
                && !executor.hasPermission(Permissions.REMOVE_OTHER)) {
            executor.sendMessage(messageRegistry.getMessage(Message.NO_PERMISSION_REMOVE_OTHERS));
            return;
        }

        plugin.debug(executor.getName() + " is removing " + shop.getVendor().getName() + "'s shop (#" + shop.getID() + ")");
        ShopRemoveEvent event = new ShopRemoveEvent(executor, shop);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Remove event cancelled (#" + shop.getID() + ")");
            return;
        }

        double creationPrice = shop.getShopType() == ShopType.ADMIN ? Config.shopCreationPriceAdmin : Config.shopCreationPriceNormal;
        if (creationPrice > 0 && Config.refundShopCreation && executor.getUniqueId().equals(shop.getVendor().getUniqueId())) {
            EconomyResponse r = plugin.getEconomy().depositPlayer(executor, shop.getLocation().getWorld().getName(), creationPrice);
            if (!r.transactionSuccess()) {
                plugin.debug("Economy transaction failed: " + r.errorMessage);
                executor.sendMessage(messageRegistry.getMessage(Message.ERROR_OCCURRED,
                        new Replacement(Placeholder.ERROR, r.errorMessage)));
                executor.sendMessage(messageRegistry.getMessage(Message.SHOP_REMOVED_REFUND,
                        new Replacement(Placeholder.CREATION_PRICE, 0)));
            } else {
                executor.sendMessage(messageRegistry.getMessage(Message.SHOP_REMOVED_REFUND,
                        new Replacement(Placeholder.CREATION_PRICE, creationPrice)));
            }
        } else {
            executor.sendMessage(messageRegistry.getMessage(Message.SHOP_REMOVED));
        }

        shopUtils.removeShop(shop, true);
        plugin.debug("Removed shop (#" + shop.getID() + ")");
    }

    /**
     * Open a shop
     * @param executor Player, who executed the command and will receive the message
     * @param shop Shop to be opened
     * @param message Whether the player should receive the {@link Message#OPENED_SHOP} message
     */
    private void open(Player executor, Shop shop, boolean message) {
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

        if (!executor.getUniqueId().equals(shop.getVendor().getUniqueId()) && !executor.hasPermission(Permissions.OPEN_OTHER)) {
            executor.sendMessage(messageRegistry.getMessage(Message.NO_PERMISSION_OPEN_OTHERS));
            return;
        }

        plugin.debug(executor.getName() + " is opening " + shop.getVendor().getName() + "'s shop (#" + shop.getID() + ")");
        ShopOpenEvent event = new ShopOpenEvent(executor, shop);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Open event cancelled (#" + shop.getID() + ")");
            return;
        }

        executor.openInventory(shop.getInventoryHolder().getInventory());
        plugin.debug("Opened shop (#" + shop.getID() + ")");
        if (message) executor.sendMessage(messageRegistry.getMessage(Message.OPENED_SHOP,
                new Replacement(Placeholder.VENDOR, shop.getVendor().getName())));
    }

    /**
     *
     * @param executor Player, who executed the command and will retrieve the information
     * @param shop Shop from which the information will be retrieved
     */
    public void info(Player executor, Shop shop) {
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

        plugin.debug(executor.getName() + " is retrieving shop info (#" + shop.getID() + ")");
        ShopInfoEvent event = new ShopInfoEvent(executor, shop);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            plugin.debug("Info event cancelled (#" + shop.getID() + ")");
            return;
        }

        Container c = (Container) shop.getLocation().getBlock().getState();
        ItemStack itemStack = shop.getProduct().getItemStack();
        int amount = Utils.getAmount(c.getInventory(), itemStack);
        int space = Utils.getFreeSpaceForItem(c.getInventory(), itemStack);

        String vendorName = (shop.getVendor().getName() == null ?
                shop.getVendor().getUniqueId().toString() : shop.getVendor().getName());

        String vendorString = messageRegistry.getMessage(Message.SHOP_INFO_VENDOR,
                new Replacement(Placeholder.VENDOR, vendorName));

        // Make JSON message with item preview
        final ShopProduct product = shop.getProduct();
        Consumer<Player> productMessage = plugin.getPlatform().getTextComponentHelper().getSendableItemInfo(
                messageRegistry.getMessage(Message.SHOP_INFO_PRODUCT,
                        new Replacement(Placeholder.AMOUNT, String.valueOf(product.getAmount()))),
                Placeholder.ITEM_NAME.toString(),
                product.getItemStack(),
                product.getLocalizedName()
        );

        String disabled = messageRegistry.getMessage(Message.SHOP_INFO_DISABLED);

        String priceString = messageRegistry.getMessage(Message.SHOP_INFO_PRICE,
                new Replacement(Placeholder.BUY_PRICE, (shop.getBuyPrice() > 0 ? String.valueOf(shop.getBuyPrice()) : disabled)),
                new Replacement(Placeholder.SELL_PRICE, (shop.getSellPrice() > 0 ? String.valueOf(shop.getSellPrice()) : disabled)));

        String shopType = messageRegistry.getMessage(shop.getShopType() == ShopType.NORMAL ?
                Message.SHOP_INFO_NORMAL : Message.SHOP_INFO_ADMIN);

        String stock = messageRegistry.getMessage(Message.SHOP_INFO_STOCK,
                new Replacement(Placeholder.STOCK, amount));

        String chestSpace = messageRegistry.getMessage(Message.SHOP_INFO_CHEST_SPACE,
                new Replacement(Placeholder.CHEST_SPACE, space));

        executor.sendMessage(" ");
        if (shop.getShopType() != ShopType.ADMIN) executor.sendMessage(vendorString);
        productMessage.accept(executor);
        if (shop.getShopType() != ShopType.ADMIN && shop.getBuyPrice() > 0) executor.sendMessage(stock);
        if (shop.getShopType() != ShopType.ADMIN && shop.getSellPrice() > 0) executor.sendMessage(chestSpace);
        executor.sendMessage(priceString);
        executor.sendMessage(shopType);
        executor.sendMessage(" ");
    }

    /**
     * A player buys from a shop
     * @param executor Player, who executed the command and will buy the product
     * @param shop Shop, from which the player buys
     * @param stack Whether a whole stack should be bought
     */
    private void buy(Player executor, final Shop shop, boolean stack) {
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

        plugin.debug(executor.getName() + " is buying (#" + shop.getID() + ")");

        ItemStack itemStack = shop.getProduct().getItemStack();
        int amount = shop.getProduct().getAmount();
        if (stack && amount<=itemStack.getMaxStackSize()) amount = itemStack.getMaxStackSize();

        String worldName = shop.getLocation().getWorld().getName();

        double price = shop.getBuyPrice();
        if (stack) price = (price / shop.getProduct().getAmount()) * amount;

        if (econ.getBalance(executor, worldName) >= price || Config.autoCalculateItemAmount) {

            int amountForMoney = (int) (amount / price * econ.getBalance(executor, worldName));

            if (amountForMoney == 0 && Config.autoCalculateItemAmount) {
                executor.sendMessage(messageRegistry.getMessage(Message.NOT_ENOUGH_MONEY));
                return;
            }

            plugin.debug(executor.getName() + " has enough money for " + amountForMoney + " item(s) (#" + shop.getID() + ")");

            Block b = shop.getLocation().getBlock();
            Container c = (Container) b.getState();

            int amountForChestItems = Utils.getAmount(c.getInventory(), itemStack);

            if (amountForChestItems == 0 && shop.getShopType() != ShopType.ADMIN) {
                executor.sendMessage(messageRegistry.getMessage(Message.OUT_OF_STOCK));
                return;
            }

            ItemStack product = new ItemStack(itemStack);
            if (stack) product.setAmount(amount);

            Inventory inventory = executor.getInventory();

            int freeSpace = Utils.getFreeSpaceForItem(inventory, product);

            if (freeSpace == 0) {
                executor.sendMessage(messageRegistry.getMessage(Message.NOT_ENOUGH_INVENTORY_SPACE));
                return;
            }

            int newAmount = amount;

            if (Config.autoCalculateItemAmount) {
                if (shop.getShopType() == ShopType.ADMIN)
                    newAmount = Math.min(amountForMoney, freeSpace);
                else
                    newAmount = Math.min(Math.min(amountForMoney, amountForChestItems), freeSpace);
            }

            if (newAmount > amount) newAmount = amount;

            ShopProduct newProduct = new ShopProduct(product, newAmount);
            double newPrice = (price / amount) * newAmount;

            if (freeSpace >= newAmount) {
                plugin.debug(executor.getName() + " has enough inventory space for " + freeSpace + " items (#" + shop.getID() + ")");

                EconomyResponse r = econ.withdrawPlayer(executor, worldName, newPrice);

                if (r.transactionSuccess()) {
                    EconomyResponse r2 = (shop.getShopType() != ShopType.ADMIN) ? econ.depositPlayer(shop.getVendor(), worldName, Config.applyTaxes(newPrice)) : null;

                    if (r2 != null) {
                        if (r2.transactionSuccess()) {
                            ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.BUY, newAmount, newPrice);
                            Bukkit.getPluginManager().callEvent(event);

                            if (event.isCancelled()) {
                                econ.depositPlayer(executor, worldName, newPrice);
                                econ.withdrawPlayer(shop.getVendor(), worldName, Config.applyTaxes(newPrice));
                                plugin.debug("Buy event cancelled (#" + shop.getID() + ")");
                                return;
                            }

                            database.logEconomy(executor, shop, newProduct, newPrice, ShopBuySellEvent.Type.BUY, null);

                            addToInventory(inventory, newProduct);
                            removeFromInventory(c.getInventory(), newProduct);
                            executor.updateInventory();

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (plugin.getHologramFormat().isDynamic()) {
                                        shop.updateHologramText();
                                    }
                                }
                            }.runTaskLater(plugin, 1L);

                            String vendorName = (shop.getVendor().getName() == null ? shop.getVendor().getUniqueId().toString() : shop.getVendor().getName());
                            executor.sendMessage(messageRegistry.getMessage(Message.BUY_SUCCESS, new Replacement(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                    new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()), new Replacement(Placeholder.BUY_PRICE, String.valueOf(newPrice)),
                                    new Replacement(Placeholder.VENDOR, vendorName)));

                            plugin.debug(executor.getName() + " successfully bought (#" + shop.getID() + ")");

                            if (shop.getVendor().isOnline() && Config.enableVendorMessages) {
                                shop.getVendor().getPlayer().sendMessage(messageRegistry.getMessage(Message.SOMEONE_BOUGHT, new Replacement(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                        new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()), new Replacement(Placeholder.BUY_PRICE, String.valueOf(newPrice)),
                                        new Replacement(Placeholder.PLAYER, executor.getName())));
                            } else if(!shop.getVendor().isOnline() && Config.enableVendorBungeeMessages){
                                String message = messageRegistry.getMessage(Message.SOMEONE_BOUGHT, new Replacement(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                        new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()), new Replacement(Placeholder.BUY_PRICE, String.valueOf(newPrice)),
                                        new Replacement(Placeholder.PLAYER, executor.getName()));
                                sendBungeeMessage(shop.getVendor().getName(),message);
                            }

                        } else {
                            plugin.debug("Economy transaction failed (r2): " + r2.errorMessage + " (#" + shop.getID() + ")");
                            executor.sendMessage(messageRegistry.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, r2.errorMessage)));
                            econ.withdrawPlayer(shop.getVendor(), worldName, Config.applyTaxes(newPrice));
                            econ.depositPlayer(executor, worldName, newPrice);
                        }
                    } else {
                        ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.BUY, newAmount, newPrice);
                        Bukkit.getPluginManager().callEvent(event);

                        if (event.isCancelled()) {
                            econ.depositPlayer(executor, worldName, newPrice);
                            plugin.debug("Buy event cancelled (#" + shop.getID() + ")");
                            return;
                        }

                        database.logEconomy(executor, shop, newProduct, newPrice, ShopBuySellEvent.Type.BUY, null);

                        addToInventory(inventory, newProduct);
                        executor.updateInventory();

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (plugin.getHologramFormat().isDynamic()) {
                                    shop.updateHologramText();
                                }
                            }
                        }.runTaskLater(plugin, 1L);

                        executor.sendMessage(messageRegistry.getMessage(Message.BUY_SUCCESS_ADMIN, new Replacement(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()), new Replacement(Placeholder.BUY_PRICE, String.valueOf(newPrice))));

                        plugin.debug(executor.getName() + " successfully bought (#" + shop.getID() + ")");
                    }
                } else {
                    plugin.debug("Economy transaction failed (r): " + r.errorMessage + " (#" + shop.getID() + ")");
                    executor.sendMessage(messageRegistry.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, r.errorMessage)));
                    econ.depositPlayer(executor, worldName, newPrice);
                }
            } else {
                executor.sendMessage(messageRegistry.getMessage(Message.NOT_ENOUGH_INVENTORY_SPACE));
            }
        } else {
            executor.sendMessage(messageRegistry.getMessage(Message.NOT_ENOUGH_MONEY));
        }
    }

    /**
     * A player sells to a shop
     * @param executor Player, who executed the command and will sell the product
     * @param shop Shop, to which the player sells
     */
    private void sell(Player executor, final Shop shop, boolean stack) {
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

        plugin.debug(executor.getName() + " is selling (#" + shop.getID() + ")");

        ItemStack itemStack = shop.getProduct().getItemStack();
        int amount = shop.getProduct().getAmount();
        if (stack && amount<= itemStack.getMaxStackSize()) amount = itemStack.getMaxStackSize();

        double price = shop.getSellPrice();
        if (stack) price = (price / shop.getProduct().getAmount()) * amount;

        String worldName = shop.getLocation().getWorld().getName();

        if (shop.getShopType() == ShopType.ADMIN || econ.getBalance(shop.getVendor(), worldName) >= price || Config.autoCalculateItemAmount) {
            int amountForMoney = 1;
            
            if (shop.getShopType() != ShopType.ADMIN) {
                 amountForMoney = (int) (amount / price * econ.getBalance(shop.getVendor(), worldName));
            }

            plugin.debug("Vendor has enough money for " + amountForMoney + " item(s) (#" + shop.getID() + ")");

            if (amountForMoney == 0 && Config.autoCalculateItemAmount && shop.getShopType() != ShopType.ADMIN) {
                executor.sendMessage(messageRegistry.getMessage(Message.VENDOR_NOT_ENOUGH_MONEY));
                return;
            }

            Block block = shop.getLocation().getBlock();
            Container chest = (Container) block.getState();

            int amountForItemCount = Utils.getAmount(executor.getInventory(), itemStack);

            if (amountForItemCount == 0) {
                executor.sendMessage(messageRegistry.getMessage(Message.NOT_ENOUGH_ITEMS));
                return;
            }

            ItemStack product = new ItemStack(itemStack);
            if (stack) product.setAmount(amount);

            Inventory inventory = chest.getInventory();

            int freeSpace = Utils.getFreeSpaceForItem(inventory, product);

            if (freeSpace == 0 && shop.getShopType() != ShopType.ADMIN) {
                executor.sendMessage(messageRegistry.getMessage(Message.CHEST_NOT_ENOUGH_INVENTORY_SPACE));
                return;
            }

            int newAmount = amount;

            if (Config.autoCalculateItemAmount) {
                if (shop.getShopType() == ShopType.ADMIN)
                    newAmount = amountForItemCount;
                else
                    newAmount = Math.min(Math.min(amountForMoney, amountForItemCount), freeSpace);
            }

            if (newAmount > amount) newAmount = amount;

            ShopProduct newProduct = new ShopProduct(product, newAmount);
            double newPrice = (price / amount) * newAmount;

            if (freeSpace >= newAmount || shop.getShopType() == ShopType.ADMIN) {
                plugin.debug("Chest has enough inventory space for " + freeSpace + " items (#" + shop.getID() + ")");

                EconomyResponse r = econ.depositPlayer(executor, worldName, Config.applyTaxes(newPrice));

                if (r.transactionSuccess()) {
                    EconomyResponse r2 = (shop.getShopType() != ShopType.ADMIN) ? econ.withdrawPlayer(shop.getVendor(), worldName, newPrice) : null;

                    if (r2 != null) {
                        if (r2.transactionSuccess()) {
                            ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.SELL, newAmount, newPrice);
                            Bukkit.getPluginManager().callEvent(event);

                            if (event.isCancelled()) {
                                econ.withdrawPlayer(executor, worldName, Config.applyTaxes(newPrice));
                                econ.depositPlayer(shop.getVendor(), worldName, newPrice);
                                plugin.debug("Sell event cancelled (#" + shop.getID() + ")");
                                return;
                            }

                            database.logEconomy(executor, shop, newProduct, newPrice, ShopBuySellEvent.Type.SELL, null);

                            addToInventory(inventory, newProduct);
                            removeFromInventory(executor.getInventory(), newProduct);
                            executor.updateInventory();

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (plugin.getHologramFormat().isDynamic()) {
                                        shop.updateHologramText();
                                    }
                                }
                            }.runTaskLater(plugin, 1L);

                            String vendorName = (shop.getVendor().getName() == null ? shop.getVendor().getUniqueId().toString() : shop.getVendor().getName());
                            executor.sendMessage(messageRegistry.getMessage(Message.SELL_SUCCESS, new Replacement(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                    new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()), new Replacement(Placeholder.SELL_PRICE, String.valueOf(newPrice)),
                                    new Replacement(Placeholder.VENDOR, vendorName)));

                            plugin.debug(executor.getName() + " successfully sold (#" + shop.getID() + ")");

                            if (shop.getVendor().isOnline() && Config.enableVendorMessages) {
                                shop.getVendor().getPlayer().sendMessage(messageRegistry.getMessage(Message.SOMEONE_SOLD, new Replacement(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                        new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()), new Replacement(Placeholder.SELL_PRICE, String.valueOf(newPrice)),
                                        new Replacement(Placeholder.PLAYER, executor.getName())));
                            } else if(!shop.getVendor().isOnline() && Config.enableVendorBungeeMessages){
                                String message = messageRegistry.getMessage(Message.SOMEONE_SOLD, new Replacement(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                        new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()), new Replacement(Placeholder.SELL_PRICE, String.valueOf(newPrice)),
                                        new Replacement(Placeholder.PLAYER, executor.getName()));
                                sendBungeeMessage(shop.getVendor().getName(),message);
                            }

                        } else {
                            plugin.debug("Economy transaction failed (r2): " + r2.errorMessage + " (#" + shop.getID() + ")");
                            executor.sendMessage(messageRegistry.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, r2.errorMessage)));
                            econ.withdrawPlayer(executor, worldName, Config.applyTaxes(newPrice));
                            econ.depositPlayer(shop.getVendor(), worldName, newPrice);
                        }

                    } else {
                        ShopBuySellEvent event = new ShopBuySellEvent(executor, shop, ShopBuySellEvent.Type.SELL, newAmount, newPrice);
                        Bukkit.getPluginManager().callEvent(event);

                        if (event.isCancelled()) {
                            econ.withdrawPlayer(executor, worldName, newPrice);
                            plugin.debug("Sell event cancelled (#" + shop.getID() + ")");
                            return;
                        }

                        database.logEconomy(executor, shop, newProduct, newPrice, ShopBuySellEvent.Type.SELL, null);

                        removeFromInventory(executor.getInventory(), newProduct);
                        executor.updateInventory();

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (plugin.getHologramFormat().isDynamic()) {
                                    shop.updateHologramText();
                                }
                            }
                        }.runTaskLater(plugin, 1L);

                        executor.sendMessage(messageRegistry.getMessage(Message.SELL_SUCCESS_ADMIN, new Replacement(Placeholder.AMOUNT, String.valueOf(newAmount)),
                                new Replacement(Placeholder.ITEM_NAME, newProduct.getLocalizedName()), new Replacement(Placeholder.SELL_PRICE, String.valueOf(newPrice))));

                        plugin.debug(executor.getName() + " successfully sold (#" + shop.getID() + ")");
                    }

                } else {
                    plugin.debug("Economy transaction failed (r): " + r.errorMessage + " (#" + shop.getID() + ")");
                    executor.sendMessage(messageRegistry.getMessage(Message.ERROR_OCCURRED, new Replacement(Placeholder.ERROR, r.errorMessage)));
                    econ.withdrawPlayer(executor, worldName, newPrice);
                }

            } else {
                executor.sendMessage(messageRegistry.getMessage(Message.CHEST_NOT_ENOUGH_INVENTORY_SPACE));
            }

        } else {
            executor.sendMessage(messageRegistry.getMessage(Message.VENDOR_NOT_ENOUGH_MONEY));
        }
    }

    /**
     * Adds items to an inventory
     * @param inventory The inventory, to which the items will be added
     * @param itemStack Items to add
     * @return Whether all items were added to the inventory
     */
    private boolean addToInventory(Inventory inventory, ShopProduct product) {
        plugin.debug("Adding items to inventory...");

        HashMap<Integer, ItemStack> inventoryItems = new HashMap<>();
        ItemStack itemStack = product.getItemStack();
        int amount = product.getAmount();
        int added = 0;

        if (inventory instanceof PlayerInventory) {
            if (Utils.getMajorVersion() >= 9) {
                inventoryItems.put(40, inventory.getItem(40));
            }

            for (int i = 0; i < 36; i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }

        } else {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }
        }

        slotLoop:
        for (int slot : inventoryItems.keySet()) {
            while (added < amount) {
                ItemStack item = inventory.getItem(slot);

                if (item != null && item.getType() != Material.AIR) {
                    if (Utils.isItemSimilar(item, itemStack)) {
                        if (item.getAmount() != item.getMaxStackSize()) {
                            ItemStack newItemStack = new ItemStack(item);
                            newItemStack.setAmount(item.getAmount() + 1);
                            inventory.setItem(slot, newItemStack);
                            added++;
                        } else {
                            continue slotLoop;
                        }
                    } else {
                        continue slotLoop;
                    }
                } else {
                    ItemStack newItemStack = new ItemStack(itemStack);
                    newItemStack.setAmount(1);
                    inventory.setItem(slot, newItemStack);
                    added++;
                }
            }
        }

        return (added == amount);
    }

    /**
     * Removes items to from an inventory
     * @param inventory The inventory, from which the items will be removed
     * @param itemStack Items to remove
     * @return Whether all items were removed from the inventory
     */
    private boolean removeFromInventory(Inventory inventory, ShopProduct product) {
        plugin.debug("Removing items from inventory...");

        HashMap<Integer, ItemStack> inventoryItems = new HashMap<>();
        ItemStack itemStack = product.getItemStack();
        int amount = product.getAmount();
        int removed = 0;

        if (inventory instanceof PlayerInventory) {
            if (Utils.getMajorVersion() >= 9) {
                inventoryItems.put(40, inventory.getItem(40));
            }

            for (int i = 0; i < 36; i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }

        } else {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventoryItems.put(i, inventory.getItem(i));
            }
        }

        slotLoop:
        for (int slot : inventoryItems.keySet()) {
            while (removed < amount) {
                ItemStack item = inventory.getItem(slot);

                if (item != null && item.getType() != Material.AIR) {
                    if (Utils.isItemSimilar(item, itemStack)) {
                        if (item.getAmount() > 0) {
                            int newAmount = item.getAmount() - 1;

                            ItemStack newItemStack = new ItemStack(item);
                            newItemStack.setAmount(newAmount);

                            if (newAmount == 0)
                                inventory.setItem(slot, null);
                            else
                                inventory.setItem(slot, newItemStack);

                            removed++;
                        } else {
                            continue slotLoop;
                        }
                    } else {
                        continue slotLoop;
                    }
                } else {
                    continue slotLoop;
                }

            }
        }

        return (removed == amount);
    }

    public void sendBungeeMessage(String player, String message) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);

            out.writeUTF("Message");
            out.writeUTF(player);
            out.writeUTF(message);

            if (!plugin.getServer().getOnlinePlayers().isEmpty()) {
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    Player p = plugin.getServer().getOnlinePlayers().iterator().next();
                    p.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
                });
            }
        } catch (Exception e) {
            plugin.debug("Failed to send bungee message");
            plugin.debug(e);
            plugin.getLogger().warning("Failed to send BungeeCord message");
        }
    }

    private double getBuyPrice(final Shop shop, boolean stack)
    {
        if(shop.getProduct().getAmount() >= shop.getProduct().getItemStack().getMaxStackSize())
            return shop.getBuyPrice();
        if(stack)
            return (shop.getBuyPrice() / shop.getProduct().getAmount()) * shop.getProduct().getItemStack().getMaxStackSize();
        else
            return shop.getBuyPrice();
    }

    private double getSellPrice(final Shop shop, boolean stack)
    {
        if(shop.getProduct().getAmount() >= shop.getProduct().getItemStack().getMaxStackSize())
            return shop.getSellPrice();
        if(stack)
            return (shop.getSellPrice() / shop.getProduct().getAmount()) * shop.getProduct().getItemStack().getMaxStackSize();
        else
            return shop.getSellPrice();
    }

    protected void performModify(final Player p, final ClickType.ModifyClickType modifyClickType, final Shop shop) {
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

        ItemStack itemStack = shop.getProduct().getItemStack();
        int amount = modifyClickType.getAmount();
        double buyPrice = modifyClickType.getBuyPrice();
        double sellPrice = modifyClickType.getSellPrice();
        boolean buyEnabled = buyPrice > 0;
        boolean sellEnabled = sellPrice > 0;
        ShopType shopType = shop.getShopType();

        // Check if item on blacklist
        for (String item :Config.blacklist) {
            ItemStack is = ItemUtils.getItemStack(item);

            if (is == null) {
                plugin.getLogger().warning("Invalid item found in blacklist: " + item);
                plugin.debug("Invalid item in blacklist: " + item);
                continue;
            }

            if (is.getType().equals(itemStack.getType()) && is.getDurability() == itemStack.getDurability()) {
                p.sendMessage(messageRegistry.getMessage(Message.CANNOT_SELL_ITEM));
                plugin.debug(p.getName() + "'s item is on the blacklist");
                return;
            }
        }

        // Check if prices lower than minimum price
        for (String key :Config.minimumPrices) {
            ItemStack is = ItemUtils.getItemStack(key);
            double minPrice = plugin.getConfig().getDouble("minimum-prices." + key);

            if (is == null) {
                plugin.getLogger().warning("Invalid item found in minimum-prices: " + key);
                plugin.debug("Invalid item in minimum-prices: " + key);
                continue;
            }

            if (is.getType().equals(itemStack.getType()) && is.getDurability() == itemStack.getDurability()) {
                if (buyEnabled) {
                    if ((buyPrice < amount * minPrice) && (buyPrice > 0)) {
                        p.sendMessage(messageRegistry.getMessage(Message.BUY_PRICE_TOO_LOW, new Replacement(Placeholder.MIN_PRICE, String.valueOf(amount * minPrice))));
                        plugin.debug(p.getName() + "'s buy price is lower than the minimum");
                        return;
                    }
                }

                if (sellEnabled) {
                    if ((sellPrice < amount * minPrice) && (sellPrice > 0)) {
                        p.sendMessage(messageRegistry.getMessage(Message.SELL_PRICE_TOO_LOW, new Replacement(Placeholder.MIN_PRICE, String.valueOf(amount * minPrice))));
                        plugin.debug(p.getName() + "'s sell price is lower than the minimum");
                        return;
                    }
                }
            }
        }

        // Check if prices higher than maximum price
        for (String key :Config.maximumPrices) {
            ItemStack is = ItemUtils.getItemStack(key);
            double maxPrice = plugin.getConfig().getDouble("maximum-prices." + key);

            if (is == null) {
                plugin.getLogger().warning("Invalid item found in maximum-prices: " + key);
                plugin.debug("Invalid item in maximum-prices: " + key);
                continue;
            }

            if (is.getType().equals(itemStack.getType()) && is.getDurability() == itemStack.getDurability()) {
                if (buyEnabled) {
                    if ((buyPrice > amount * maxPrice) && (buyPrice > 0)) {
                        p.sendMessage(messageRegistry.getMessage(Message.BUY_PRICE_TOO_HIGH, new Replacement(Placeholder.MAX_PRICE, String.valueOf(amount * maxPrice))));
                        plugin.debug(p.getName() + "'s buy price is higher than the maximum");
                        return;
                    }
                }

                if (sellEnabled) {
                    if ((sellPrice > amount * maxPrice) && (sellPrice > 0)) {
                        p.sendMessage(messageRegistry.getMessage(Message.SELL_PRICE_TOO_HIGH, new Replacement(Placeholder.MAX_PRICE, String.valueOf(amount * maxPrice))));
                        plugin.debug(p.getName() + "'s sell price is higher than the maximum");
                        return;
                    }
                }
            }
        }


        if (sellEnabled && buyEnabled) {
            if (Config.buyGreaterOrEqualSell) {
                if (buyPrice < sellPrice) {
                    p.sendMessage(messageRegistry.getMessage(Message.BUY_PRICE_TOO_LOW, new Replacement(Placeholder.MIN_PRICE, String.valueOf(sellPrice))));
                    plugin.debug(p.getName() + "'s buy price is lower than the sell price");
                    return;
                }
            }
        }

        if (plugin.getUNBREAKING_ENCHANT().canEnchantItem(itemStack)) {
            if (itemStack.getDurability() > 0 && !Config.allowBrokenItems) {
                p.sendMessage(messageRegistry.getMessage(Message.CANNOT_SELL_BROKEN_ITEM));
                plugin.debug(p.getName() + "'s item is broken");
                return;
            }
        }

        double modifyPrice = (shopType == Shop.ShopType.NORMAL) ?Config.shopModifyPriceNormal :Config.shopModifyPriceAdmin;
        if (modifyPrice > 0) {
            if (plugin.getEconomy().getBalance(p, p.getWorld().getName()) < modifyPrice) {
                p.sendMessage(messageRegistry.getMessage(Message.SHOP_MODIFY_NOT_ENOUGH_MONEY, new Replacement(Placeholder.MODIFY_PRICE, String.valueOf(modifyPrice))));
                plugin.debug(p.getName() + " can not pay the creation price");
                return;
            }
        }

        ShopProduct product = new ShopProduct(itemStack, amount);
        ShopModifyEvent event = new ShopModifyEvent(p, new Shop(plugin, p, product, shop.getLocation(), buyPrice, sellPrice, shopType));
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            final Shop newShop = new Shop(
                    shop.getID(),
                    plugin,
                    shop.getVendor(),
                    product,
                    shop.getLocation(),
                    buyPrice,
                    sellPrice,
                    shop.getShopType()
            );
            plugin.getShopUtils().removeShop(shop, false, null);
            newShop.create(true);
            plugin.getShopUtils().modifyShop(newShop,
                    true,
                    null
            );

            EconomyResponse r = plugin.getEconomy().withdrawPlayer(p, shop.getLocation().getWorld().getName(), modifyPrice);
            p.sendMessage(messageRegistry.getMessage(Message.SHOP_MODIFIED, new Replacement(Placeholder.MODIFY_PRICE, String.valueOf(modifyPrice))));

            plugin.debug(p.getName() + " can not pay the creation price");

        } else {
            plugin.debug("Shop modify event cancelled");
        }
    }
}
