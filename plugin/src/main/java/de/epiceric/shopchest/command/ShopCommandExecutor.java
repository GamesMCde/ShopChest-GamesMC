package de.epiceric.shopchest.command;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.config.Placeholder;
import de.epiceric.shopchest.event.*;
import de.epiceric.shopchest.language.Message;
import de.epiceric.shopchest.language.MessageRegistry;
import de.epiceric.shopchest.language.Replacement;
import de.epiceric.shopchest.shop.Shop;
import de.epiceric.shopchest.shop.Shop.ShopType;
import de.epiceric.shopchest.shop.ShopProduct;
import de.epiceric.shopchest.utils.*;
import de.epiceric.shopchest.utils.ClickType.CreateClickType;
import de.epiceric.shopchest.utils.ClickType.SelectClickType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

class ShopCommandExecutor implements CommandExecutor {

    private ShopChest plugin;
    private ShopUtils shopUtils;
    private final Enchantment UNBREAKING_ENCHANT;

    ShopCommandExecutor(ShopChest plugin) {
        this.plugin = plugin;
        this.shopUtils = plugin.getShopUtils();
        UNBREAKING_ENCHANT = loadUnbreakingEnchant();
    }

    private Enchantment loadUnbreakingEnchant() {
        // The constant name changed in 1.20.5
        // Doing this ensure compatibility with older version when using older version
        try {
            final Field field = Enchantment.class.getDeclaredField("DURABILITY");
            field.setAccessible(true);
            return (Enchantment) field.get(null);
        } catch (ReflectiveOperationException e) {
            return Enchantment.UNBREAKING;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();
        List<ShopSubCommand> subCommands = plugin.getShopCommand().getSubCommands();

        if (args.length > 0) {
            String _subCommand = args[0];
            ShopSubCommand subCommand = null;

            for (ShopSubCommand shopSubCommand : subCommands) {
                if (shopSubCommand.getName().equalsIgnoreCase(_subCommand)) {
                    subCommand = shopSubCommand;
                    break;
                }
            }

            if (subCommand == null) {
                return false;
            }

            if (subCommand.getName().equalsIgnoreCase("reload")) {
                if (sender.hasPermission(Permissions.RELOAD)) {
                    reload(sender);
                } else {
                    sender.sendMessage(messageRegistry.getMessage(Message.NO_PERMISSION_RELOAD));
                }
            } else if (subCommand.getName().equalsIgnoreCase("update")) {
                if (sender.hasPermission(Permissions.UPDATE)) {
                    checkUpdates(sender);
                } else {
                    sender.sendMessage(messageRegistry.getMessage(Message.NO_PERMISSION_UPDATE));
                }
            } else if (subCommand.getName().equalsIgnoreCase("config")) {
                if (sender.hasPermission(Permissions.CONFIG)) {
                    return args.length >= 4 && changeConfig(sender, args);
                } else {
                    sender.sendMessage(messageRegistry.getMessage(Message.NO_PERMISSION_CONFIG));
                }
            } else if (subCommand.getName().equalsIgnoreCase("removeall")) {
                if (sender.hasPermission(Permissions.REMOVE_OTHER)) {
                    if (args.length >= 2) {
                        removeAll(sender, args);
                    } else {
                        return false;
                    }
                } else {
                    sender.sendMessage(messageRegistry.getMessage(Message.NO_PERMISSION_REMOVE_OTHERS));
                }
            } else {
                if (sender instanceof Player) {
                    Player p = (Player) sender;

                    if (subCommand.getName().equalsIgnoreCase("create")) {
                        if (args.length == 4) {
                            create(args, Shop.ShopType.NORMAL, p);
                        } else if (args.length == 5) {
                            if (args[4].equalsIgnoreCase("normal")) {
                                create(args, Shop.ShopType.NORMAL, p);
                            } else if (args[4].equalsIgnoreCase("admin")) {
                                if (p.hasPermission(Permissions.CREATE_ADMIN)) {
                                    create(args, Shop.ShopType.ADMIN, p);
                                } else {
                                    p.sendMessage(messageRegistry.getMessage(Message.NO_PERMISSION_CREATE_ADMIN));
                                }
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    } else if (subCommand.getName().equalsIgnoreCase("remove")) {
                        remove(p);
                    } else if (subCommand.getName().equalsIgnoreCase("info")) {
                        info(p);
                    } else if (subCommand.getName().equalsIgnoreCase("list")) {
                        list(p, command.getName(), args);
                    }  else if (subCommand.getName().equalsIgnoreCase("limits")) {
                        plugin.debug(p.getName() + " is viewing his shop limits: " + shopUtils.getShopAmount(p) + "/" + shopUtils.getShopLimit(p));
                        int limit = shopUtils.getShopLimit(p);
                        p.sendMessage(messageRegistry.getMessage(Message.OCCUPIED_SHOP_SLOTS,
                                new Replacement(Placeholder.LIMIT, (limit < 0 ? "∞" : String.valueOf(limit))),
                                new Replacement(Placeholder.AMOUNT, String.valueOf(shopUtils.getShopAmount(p)))));
                    } else if (subCommand.getName().equalsIgnoreCase("open")) {
                        open(p);
                    } else {
                        return false;
                    }
                }
            }

            return true;
        }

        return false;
    }

    /**
     * A given player checks for updates
     * @param sender The command executor
     */
    private void checkUpdates(CommandSender sender) {
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

        plugin.debug(sender.getName() + " is checking for updates");

        sender.sendMessage(messageRegistry.getMessage(Message.UPDATE_CHECKING));

        UpdateChecker uc = new UpdateChecker(ShopChest.getInstance());
        UpdateChecker.UpdateCheckerResult result = uc.check();

        if (result == UpdateChecker.UpdateCheckerResult.TRUE) {
            plugin.setLatestVersion(uc.getVersion());
            plugin.setDownloadLink(uc.getLink());
            plugin.setUpdateNeeded(true);

            if (sender instanceof Player) {
                Utils.sendUpdateMessage(plugin, (Player) sender);
            } else {
                sender.sendMessage(messageRegistry.getMessage(Message.UPDATE_AVAILABLE, new Replacement(Placeholder.VERSION, uc.getVersion())));
            }

        } else if (result == UpdateChecker.UpdateCheckerResult.FALSE) {
            plugin.setLatestVersion("");
            plugin.setDownloadLink("");
            plugin.setUpdateNeeded(false);
            sender.sendMessage(messageRegistry.getMessage(Message.UPDATE_NO_UPDATE));
        } else {
            plugin.setLatestVersion("");
            plugin.setDownloadLink("");
            plugin.setUpdateNeeded(false);
            sender.sendMessage(messageRegistry.getMessage(Message.UPDATE_ERROR));
        }
    }

    /**
     * A given player reloads the shops
     * @param sender The command executor
     */
    private void reload(final CommandSender sender) {
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

        plugin.debug(sender.getName() + " is reloading the shops");

        ShopReloadEvent event = new ShopReloadEvent(sender);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Reload event cancelled");
            return;
        }

        // Reload configurations
        plugin.getShopChestConfig().reload(false, true, true);
        plugin.getHologramFormat().reload();
        plugin.getUpdater().restart();

        // Remove all shops
        for (Shop shop : shopUtils.getShops()) {
            shopUtils.removeShop(shop, false);
        }

        Chunk[] loadedChunks = Bukkit.getWorlds().stream().map(World::getLoadedChunks)
                .flatMap(Stream::of).toArray(Chunk[]::new);

        // Reconnect to the database and re-load shops in loaded chunks
        plugin.getShopDatabase().connect(new Callback<Integer>(plugin) {
            @Override
            public void onResult(Integer result) {
                shopUtils.loadShops(loadedChunks, new Callback<Integer>(plugin) {
                    @Override
                    public void onResult(Integer result) {
                        sender.sendMessage(messageRegistry.getMessage(Message.RELOADED_SHOPS,
                                new Replacement(Placeholder.AMOUNT, String.valueOf(result))));
                        plugin.debug(sender.getName() + " has reloaded " + result + " shops");
                    }
        
                    @Override
                    public void onError(Throwable throwable) {
                        sender.sendMessage(messageRegistry.getMessage(Message.ERROR_OCCURRED,
                                new Replacement(Placeholder.ERROR, "Failed to load shops from database")));
                        plugin.getLogger().severe("Failed to load shops");
                        if (throwable != null) plugin.getLogger().severe(throwable.getMessage());
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                // Database connection probably failed => disable plugin to prevent more errors
                sender.sendMessage(messageRegistry.getMessage(Message.ERROR_OCCURRED,
                        new Replacement(Placeholder.ERROR, "No database access: Disabling ShopChest")));
                plugin.getLogger().severe("No database access: Disabling ShopChest");
                if (throwable != null) plugin.getLogger().severe(throwable.getMessage());
                plugin.getServer().getPluginManager().disablePlugin(plugin);
            }
        });
    }

    /**
     * A given player creates a shop
     * @param args Arguments of the entered command
     * @param shopType The {@link Shop.ShopType}, the shop will have
     * @param p The command executor
     */
    private void create(String[] args, Shop.ShopType shopType, final Player p) {
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

        plugin.debug(p.getName() + " wants to create a shop");

        int amount;
        double buyPrice, sellPrice;

        // Check if amount and prices are valid
        try {
            amount = Integer.parseInt(args[1]);
            buyPrice = Double.parseDouble(args[2]);
            sellPrice = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            p.sendMessage(messageRegistry.getMessage(Message.AMOUNT_PRICE_NOT_NUMBER));
            plugin.debug(p.getName() + " has entered an invalid amount and/or prices");
            return;
        }

        if (!Utils.hasPermissionToCreateShop(p, Utils.getPreferredItemInHand(p), buyPrice > 0, sellPrice > 0)) {
            p.sendMessage(messageRegistry.getMessage(Message.NO_PERMISSION_CREATE));
            plugin.debug(p.getName() + " is not permitted to create the shop");
            return;
        }

        // Check for limits
        int limit = shopUtils.getShopLimit(p);
        if (limit != -1) {
            if (shopUtils.getShopAmount(p) >= limit) {
                if (shopType != Shop.ShopType.ADMIN) {
                    p.sendMessage(messageRegistry.getMessage(Message.SHOP_LIMIT_REACHED, new Replacement(Placeholder.LIMIT, String.valueOf(limit))));
                    plugin.debug(p.getName() + " has reached the limit");
                    return;
                }
            }
        }

        if (amount <= 0) {
            p.sendMessage(messageRegistry.getMessage(Message.AMOUNT_IS_ZERO));
            plugin.debug(p.getName() + " has entered an invalid amount");
            return;
        }

        if (!Config.allowDecimalsInPrice && (buyPrice != (int) buyPrice || sellPrice != (int) sellPrice)) {
            p.sendMessage(messageRegistry.getMessage(Message.PRICES_CONTAIN_DECIMALS));
            plugin.debug(p.getName() + " has entered an invalid price");
            return;
        }

        boolean buyEnabled = buyPrice > 0;
        boolean sellEnabled = sellPrice > 0;

        if (!buyEnabled && !sellEnabled) {
            p.sendMessage(messageRegistry.getMessage(Message.BUY_SELL_DISABLED));
            plugin.debug(p.getName() + " has disabled buying and selling");
            return;
        }

        ItemStack inHand = Utils.getPreferredItemInHand(p);

        // Check if item in hand
        if (inHand == null) {
            plugin.debug(p.getName() + " does not have an item in his hand");

            if (!Config.creativeSelectItem) {
                p.sendMessage(messageRegistry.getMessage(Message.NO_ITEM_IN_HAND));
                return;
            }

            if (!(ClickType.getPlayerClickType(p) instanceof SelectClickType)) {
                // Don't set previous game mode to creative if player already has select click type
                ClickType.setPlayerClickType(p, new SelectClickType(p.getGameMode(), amount, buyPrice, sellPrice, shopType));
                p.setGameMode(GameMode.CREATIVE);
            }

            p.sendMessage(messageRegistry.getMessage(Message.SELECT_ITEM));
        } else {
            SelectClickType ct = new SelectClickType(null, amount, buyPrice, sellPrice, shopType);
            ct.setItem(inHand);
            create2(p, ct);
        }
    }

    /**
     * <b>SHALL ONLY BE CALLED VIA {@link ShopCommand#createShopAfterSelected(Player player, SelectClickType clickType)}</b>
     */
    protected void create2(Player p, SelectClickType selectClickType) {
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

        ItemStack itemStack = selectClickType.getItem();
        int amount = selectClickType.getAmount();
        double buyPrice = selectClickType.getBuyPrice();
        double sellPrice = selectClickType.getSellPrice();
        boolean buyEnabled = buyPrice > 0;
        boolean sellEnabled = sellPrice > 0;
        ShopType shopType = selectClickType.getShopType();

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

        if (UNBREAKING_ENCHANT.canEnchantItem(itemStack)) {
            if (itemStack.getDurability() > 0 && !Config.allowBrokenItems) {
                p.sendMessage(messageRegistry.getMessage(Message.CANNOT_SELL_BROKEN_ITEM));
                plugin.debug(p.getName() + "'s item is broken");
                return;
            }
        }

        double creationPrice = (shopType == Shop.ShopType.NORMAL) ?Config.shopCreationPriceNormal :Config.shopCreationPriceAdmin;
        if (creationPrice > 0) {
            if (plugin.getEconomy().getBalance(p, p.getWorld().getName()) < creationPrice) {
                p.sendMessage(messageRegistry.getMessage(Message.SHOP_CREATE_NOT_ENOUGH_MONEY, new Replacement(Placeholder.CREATION_PRICE, String.valueOf(creationPrice))));
                plugin.debug(p.getName() + " can not pay the creation price");
                return;
            }
        }

        ShopProduct product = new ShopProduct(itemStack, amount);
        ShopPreCreateEvent event = new ShopPreCreateEvent(p, new Shop(plugin, p, product, null, buyPrice, sellPrice, shopType));
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            ClickType.setPlayerClickType(p, new CreateClickType(product, buyPrice, sellPrice, shopType));
            plugin.debug(p.getName() + " can now click a chest");
            p.sendMessage(messageRegistry.getMessage(Message.CLICK_CHEST_CREATE));
        } else {
            plugin.debug("Shop pre create event cancelled");
        }
    }

    /**
     * A given player removes a shop
     * @param p The command executor
     */
    private void remove(final Player p) {
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

        plugin.debug(p.getName() + " wants to remove a shop");

        ShopPreRemoveEvent event = new ShopPreRemoveEvent(p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Shop pre remove event cancelled");
            return;
        }

        plugin.debug(p.getName() + " can now click a chest");
        p.sendMessage(messageRegistry.getMessage(Message.CLICK_CHEST_REMOVE));
        ClickType.setPlayerClickType(p, new ClickType(ClickType.EnumClickType.REMOVE));
    }

    /**
     * A given player retrieves information about a shop
     * @param p The command executor
     */
    private void info(final Player p) {
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

        plugin.debug(p.getName() + " wants to retrieve information");

        ShopPreInfoEvent event = new ShopPreInfoEvent(p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Shop pre info event cancelled");
            return;
        }

        plugin.debug(p.getName() + " can now click a chest");
        p.sendMessage(messageRegistry.getMessage(Message.CLICK_CHEST_INFO));
        ClickType.setPlayerClickType(p, new ClickType(ClickType.EnumClickType.INFO));
    }

    /**
     * A given player retrieves information about all shops
     * @param p The command executor
     */
    private void list(final Player p, final String command, final String[] args) {
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

        plugin.debug(p.getName() + " wants to retrieve information about all shops");

        //Load all shops of the player
        //Get offline player from the player

        plugin.getShopUtils().getShops(p, new Callback<Collection<Shop>>(plugin) {
            @Override
            public void onResult(Collection<Shop> result) {
                List<Shop> shops = new ArrayList<>(result);
                final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

                if(args.length == 1 || args.length == 2){
                    int page = 1;
                    if(args.length == 2){
                        try{
                            page = Integer.parseInt(args[1]);
                        }
                        catch (NumberFormatException e){
                            // Invalid page number, default to 1
                        }
                    }
                    int totalPages = (int) Math.ceil((double) shops.size() / Config.shopsPerPage);
                    if (page < 1 || page > totalPages) {
                        p.sendMessage(messageRegistry.getMessage(Message.INVALID_PAGE_NUMBER, new Replacement(Placeholder.PAGE, String.valueOf(page)), new Replacement(Placeholder.TOTAL_PAGES, String.valueOf(totalPages))));
                        return;
                    }
                    int startIndex = (page - 1) * Config.shopsPerPage;
                    int endIndex = Math.min(startIndex + Config.shopsPerPage, shops.size());
                    p.sendMessage(messageRegistry.getMessage(Message.SHOPS_LIST_HEADER,
                            new Replacement(Placeholder.TOTAL_SHOPS, String.valueOf(shops.size())),
                            new Replacement(Placeholder.PAGE, String.valueOf(page)),
                            new Replacement(Placeholder.TOTAL_PAGES, String.valueOf(totalPages))
                    ));

                    for (int i = startIndex; i < endIndex; i++) {
                        p.spigot().sendMessage(getShortShopInfo(shops.get(i)));
                    }

                    //Send previous and next page buttons if there are more pages and make them clickable with command /shops list page +/- 1
                    BaseComponent pageButtons = getPageButtons("/" + command + " list", page, page > 1, page < totalPages);
                    if(pageButtons != null) {
                        p.spigot().sendMessage(pageButtons);
                    }
                    p.sendMessage(messageRegistry.getMessage(Message.SHOPS_LIST_FOOTER,
                            new Replacement(Placeholder.TOTAL_SHOPS, String.valueOf(shops.size())),
                            new Replacement(Placeholder.PAGE, String.valueOf(page)),
                            new Replacement(Placeholder.TOTAL_PAGES, String.valueOf(totalPages))
                    ));

                }
            }

            @Override
            public void onError(Throwable throwable) {
                final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();
                p.sendMessage(messageRegistry.getMessage(Message.ERROR_OCCURRED,
                        new Replacement(Placeholder.ERROR, "Failed to get shops")));
            }
        });
    }

    private BaseComponent getPageButtons(final String command, final int currentPage, final boolean hasPreviousPage, final boolean hasNextPage) {
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();
        if(!hasPreviousPage && !hasNextPage) {
            return null; // No buttons to show
        }
        BaseComponent pageButtons = new TextComponent(messageRegistry.getMessage(Message.PRE_PAGINATION_FILLER));
        if (hasPreviousPage) {
            TextComponent previousPageButtons = new TextComponent();
            previousPageButtons.setText(messageRegistry.getMessage(Message.PREVIOUS_PAGE));
            previousPageButtons.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command + " " + (currentPage - 1)));
            BaseComponent[] hoverText = new BaseComponent[]{
                    new TextComponent(messageRegistry.getMessage(Message.PREVIOUS_PAGE_HOVER))
            };
            previousPageButtons.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
            previousPageButtons.setColor(ChatColor.GREEN.asBungee());
            pageButtons.addExtra(previousPageButtons);
        }
        if (hasNextPage) {
            if (hasPreviousPage) {
                pageButtons.addExtra(messageRegistry.getMessage(Message.BETWEEN_PAGINATION_FILLER));
            }
            TextComponent nextPageButtons = new TextComponent();
            nextPageButtons.setText(messageRegistry.getMessage(Message.NEXT_PAGE));
            nextPageButtons.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command + " " + (currentPage + 1)));
            BaseComponent[] hoverText = new BaseComponent[]{
                    new TextComponent(messageRegistry.getMessage(Message.NEXT_PAGE_HOVER))
            };
            nextPageButtons.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
            nextPageButtons.setColor(ChatColor.GREEN.asBungee());
            if (hasPreviousPage) {
                pageButtons.addExtra(" "); // Add space between previous and next buttons
            }
            pageButtons.addExtra(nextPageButtons);
        }
        pageButtons.addExtra(new TextComponent(messageRegistry.getMessage(Message.POST_PAGINATION_FILLER)));
        return pageButtons;
    }

    private BaseComponent getShortShopInfo(final Shop shop) {
        // Create a Base Compoenent with just one line of text containing the basic shop information
        // Show prices, location, current filling, vendor and shop type in hover message
        // Only show the coordinates and shop product name in the text
        final String productName = shop.getProduct().getItemStack().getType().name();
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();
        final String shopInfoText = messageRegistry.getMessage(Message.SHOP_INFO_SHORT,
                new Replacement(Placeholder.AMOUNT, shop.getProduct().getAmount()),
                new Replacement(Placeholder.ITEM_NAME, productName),
                new Replacement(Placeholder.LOCATION, shop.getLocation().getBlockX() + ", " + shop.getLocation().getBlockY() + ", " + shop.getLocation().getBlockZ()));

        BaseComponent shopInfo = new TextComponent(shopInfoText);
        shopInfo.setColor(ChatColor.GRAY.asBungee());

        Container c = null;
        try{
            c = (Container) shop.getLocation().getBlock().getState();
        }
        catch (Exception e){
            //Invalid shop location, return the shop info without hover event
            return new TextComponent(messageRegistry.getMessage(Message.SHOP_INFO_NO_CONTAINER,
                    new Replacement(Placeholder.LOCATION, shop.getLocation().getBlockX() + ", " + shop.getLocation().getBlockY() + ", " + shop.getLocation().getBlockZ())));
        }
        ItemStack itemStack = shop.getProduct().getItemStack();
        int amount = Utils.getAmount(c.getInventory(), itemStack);
        int space = Utils.getFreeSpaceForItem(c.getInventory(), itemStack);

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

        //Add those information in the hover message
        BaseComponent[] hoverMessage = new BaseComponent[]{
                new TextComponent(messageRegistry.getMessage(Message.SHOP_INFO_VENDOR, new Replacement(Placeholder.VENDOR, shop.getVendor().getName()))),
                new TextComponent("\n"),
                new TextComponent(priceString),
                new TextComponent("\n"),
                new TextComponent(shopType),
                new TextComponent("\n"),
                new TextComponent(stock),
                new TextComponent("\n"),
                new TextComponent(chestSpace)
        };
        // Set the hover event to show the hover message
        shopInfo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverMessage));
        return shopInfo;
    }

    /**
     * A given player opens a shop
     * @param p The command executor
     */
    private void open(final Player p) {
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

        plugin.debug(p.getName() + " wants to open a shop");

        ShopPreOpenEvent event = new ShopPreOpenEvent(p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.debug("Shop pre open event cancelled");
            return;
        }

        plugin.debug(p.getName() + " can now click a chest");
        p.sendMessage(messageRegistry.getMessage(Message.CLICK_CHEST_OPEN));
        ClickType.setPlayerClickType(p, new ClickType(ClickType.EnumClickType.OPEN));
    }

    private boolean changeConfig(CommandSender sender, String[] args) {
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

        plugin.debug(sender.getName() + " is changing the configuration");

        String property = args[2];
        String value = args[3];

        if (args[1].equalsIgnoreCase("set")) {
            plugin.getShopChestConfig().set(property, value);
            sender.sendMessage(messageRegistry.getMessage(Message.CHANGED_CONFIG_SET, new Replacement(Placeholder.PROPERTY, property), new Replacement(Placeholder.VALUE, value)));
        } else if (args[1].equalsIgnoreCase("add")) {
            plugin.getShopChestConfig().add(property, value);
            sender.sendMessage(messageRegistry.getMessage(Message.CHANGED_CONFIG_ADDED, new Replacement(Placeholder.PROPERTY, property), new Replacement(Placeholder.VALUE, value)));
        } else if (args[1].equalsIgnoreCase("remove")) {
            plugin.getShopChestConfig().remove(property, value);
            sender.sendMessage(messageRegistry.getMessage(Message.CHANGED_CONFIG_REMOVED, new Replacement(Placeholder.PROPERTY, property), new Replacement(Placeholder.VALUE, value)));
        } else {
            return false;
        }

        return true;
    }

    private void removeAll(CommandSender sender, String[] args) {
        OfflinePlayer vendor = Bukkit.getOfflinePlayer(args[1]);

        plugin.debug(sender.getName() + " is removing all shops of " + vendor.getName());

        plugin.getShopUtils().getShops(vendor, new Callback<Collection<Shop>>(plugin) {
            @Override
            public void onResult(Collection<Shop> result) {
                List<Shop> shops = new ArrayList<>(result);

                ShopRemoveAllEvent event = new ShopRemoveAllEvent(sender, vendor, shops);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    plugin.debug("Remove all event cancelled");
                    return;
                }

                for (Shop shop : shops) {
                    shopUtils.removeShop(shop, true);
                }

                final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

                sender.sendMessage(messageRegistry.getMessage(Message.ALL_SHOPS_REMOVED,
                        new Replacement(Placeholder.AMOUNT, String.valueOf(shops.size())),
                        new Replacement(Placeholder.VENDOR, vendor.getName())));
            }

            @Override
            public void onError(Throwable throwable) {
                final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();

                sender.sendMessage(messageRegistry.getMessage(Message.ERROR_OCCURRED,
                        new Replacement(Placeholder.ERROR, "Failed to get player's shops")));
            }
        });

        
    }
}
