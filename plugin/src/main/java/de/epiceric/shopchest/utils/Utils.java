package de.epiceric.shopchest.utils;

import de.epiceric.shopchest.ShopChest;
import de.epiceric.shopchest.config.Placeholder;
import de.epiceric.shopchest.language.Message;
import de.epiceric.shopchest.language.MessageRegistry;
import de.epiceric.shopchest.language.Replacement;
import de.epiceric.shopchest.nms.CustomBookMeta;
import de.epiceric.shopchest.shop.Shop;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class Utils {

    private Utils() {}

    /**
     * Check if two items are similar to each other
     * @param itemStack1 The first item
     * @param itemStack2 The second item
     * @return {@code true} if the given items are similar or {@code false} if not
     */
    public static boolean isItemSimilar(ItemStack itemStack1, ItemStack itemStack2) {
        if (itemStack1 == null || itemStack2 == null) {
            return false;
        }

        ItemMeta itemMeta1 = itemStack1.getItemMeta();
        ItemMeta itemMeta2 = itemStack2.getItemMeta();

        if (itemMeta1 instanceof BookMeta && itemMeta2 instanceof BookMeta) {
            BookMeta bookMeta1 = (BookMeta) itemStack1.getItemMeta();
            BookMeta bookMeta2 = (BookMeta) itemStack2.getItemMeta();

            if ((getMajorVersion() == 9 && getRevision() == 1) || getMajorVersion() == 8) {
                CustomBookMeta.Generation generation1 = CustomBookMeta.getGeneration(itemStack1);
                CustomBookMeta.Generation generation2 = CustomBookMeta.getGeneration(itemStack2);

                if (generation1 == null) CustomBookMeta.setGeneration(itemStack1, CustomBookMeta.Generation.ORIGINAL);
                if (generation2 == null) CustomBookMeta.setGeneration(itemStack2, CustomBookMeta.Generation.ORIGINAL);
            } else {
                if (bookMeta1.getGeneration() == null) bookMeta1.setGeneration(BookMeta.Generation.ORIGINAL);
                if (bookMeta2.getGeneration() == null) bookMeta2.setGeneration(BookMeta.Generation.ORIGINAL);
            }

            itemStack1.setItemMeta(bookMeta1);
            itemStack2.setItemMeta(bookMeta2);

            itemStack1 = decode(encode(itemStack1));
            itemStack2 = decode(encode(itemStack2));
        }

        return itemStack1.isSimilar(itemStack2);
    }

    /**
     * Gets the amount of items in an inventory
     *
     * @param inventory The inventory, in which the items are counted
     * @param itemStack The items to count
     * @return Amount of given items in the given inventory
     */
    public static int getAmount(Inventory inventory, ItemStack itemStack) {
        int amount = 0;

        ArrayList<ItemStack> inventoryItems = new ArrayList<>();

        if (inventory instanceof PlayerInventory) {
            for (int i = 0; i < 37; i++) {
                if (i == 36) {
                    if (getMajorVersion() < 9) {
                        break;
                    }
                    i = 40;
                }
                inventoryItems.add(inventory.getItem(i));
            }
        } else {
            for (int i = 0; i < inventory.getSize(); i++) {
                inventoryItems.add(inventory.getItem(i));
            }
        }

        for (ItemStack item : inventoryItems) {
            if (isItemSimilar(item, itemStack)) {
                amount += item.getAmount();
            }
        }

        return amount;
    }

    /**
     * Get the amount of the given item, that fits in the given inventory
     *
     * @param inventory Inventory, where to search for free space
     * @param itemStack Item, of which the amount that fits in the inventory should be returned
     * @return Amount of the given item, that fits in the given inventory
     */
    public static int getFreeSpaceForItem(Inventory inventory, ItemStack itemStack) {
        HashMap<Integer, Integer> slotFree = new HashMap<>();

        if (inventory instanceof PlayerInventory) {
            for (int i = 0; i < 37; i++) {
                if (i == 36) {
                    if (getMajorVersion() < 9) {
                        break;
                    }
                    i = 40;
                }

                ItemStack item = inventory.getItem(i);
                if (item == null || item.getType() == Material.AIR) {
                    slotFree.put(i, itemStack.getMaxStackSize());
                } else {
                    if (isItemSimilar(item, itemStack)) {
                        int amountInSlot = item.getAmount();
                        int amountToFullStack = itemStack.getMaxStackSize() - amountInSlot;
                        slotFree.put(i, amountToFullStack);
                    }
                }
            }
        } else {
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                if (item == null || item.getType() == Material.AIR) {
                    slotFree.put(i, itemStack.getMaxStackSize());
                } else {
                    if (isItemSimilar(item, itemStack)) {
                        int amountInSlot = item.getAmount();
                        int amountToFullStack = itemStack.getMaxStackSize() - amountInSlot;
                        slotFree.put(i, amountToFullStack);
                    }
                }
            }
        }

        int freeAmount = 0;
        for (int value : slotFree.values()) {
            freeAmount += value;
        }

        return freeAmount;
    }

    /**
     * @param p Player whose item in his main hand should be returned
     * @return {@link ItemStack} in his main hand, or {@code null} if he doesn't hold one
     */
    public static ItemStack getItemInMainHand(Player p) {
        if (getMajorVersion() < 9) {
            if (p.getItemInHand().getType() == Material.AIR)
                return null;
            else
                return p.getItemInHand();
        }

        if (p.getInventory().getItemInMainHand().getType() == Material.AIR)
            return null;
        else
            return p.getInventory().getItemInMainHand();
    }

    /**
     * @param p Player whose item in his off hand should be returned
     * @return {@link ItemStack} in his off hand, or {@code null} if he doesn't hold one or the server version is below 1.9
     */
    public static ItemStack getItemInOffHand(Player p) {
        if (getMajorVersion() < 9)
            return null;
        else if (p.getInventory().getItemInOffHand().getType() == Material.AIR)
            return null;
        else
            return p.getInventory().getItemInOffHand();
    }

    /**
     * @param p Player whose item in his hand should be returned
     * @return Item in his main hand, or the item in his off if he doesn't have one in this main hand, or {@code null}
     *         if he doesn't have one in both hands
     */
    public static ItemStack getPreferredItemInHand(Player p) {
        if (getMajorVersion() < 9)
            return getItemInMainHand(p);
        else if (getItemInMainHand(p) != null)
            return getItemInMainHand(p);
        else
            return getItemInOffHand(p);
    }

    /**
     * @param p Player to check if he has an axe in one of his hands
     * @return Whether a player has an axe in one of his hands
     */
    public static boolean hasAxeInHand(Player p) {
        List<String> axes;
        if (Utils.getMajorVersion() < 13)
            axes = Arrays.asList("WOOD_AXE", "STONE_AXE", "IRON_AXE", "GOLD_AXE", "DIAMOND_AXE");
        else 
            axes = Arrays.asList("WOODEN_AXE", "STONE_AXE", "IRON_AXE", "GOLDEN_AXE", "DIAMOND_AXE");

        ItemStack item = getItemInMainHand(p);
        if (item == null || !axes.contains(item.getType().toString())) {
            item = getItemInOffHand(p);
        }

        return item != null && axes.contains(item.getType().toString());
    }

    /**
     * <p>Check if a player is allowed to create a shop that sells or buys the given item.</p>
     * @param player Player to check
     * @param item Item to be sold or bought
     * @param buy Whether buying should be enabled
     * @param sell Whether selling should be enabled
     * @return Whether the player is allowed
     */
    public static boolean hasPermissionToCreateShop(Player player, ItemStack item, boolean buy, boolean sell) {
        if (hasPermissionToCreateShop(player, item, Permissions.CREATE)) {
            return true;
        } else if (!sell && buy && hasPermissionToCreateShop(player, item,Permissions.CREATE_BUY)) {
            return true;
        } else if (!buy && sell && hasPermissionToCreateShop(player, item, Permissions.CREATE_SELL)) {
            return true;
        } else if (buy && sell && hasPermissionToCreateShop(player, item, Permissions.CREATE_BUY, Permissions.CREATE_SELL)) {
            return true;
        }

        return false;
    }

    private static boolean hasPermissionToCreateShop(Player player, ItemStack item, String... permissions) {
        for (String permission : permissions) {
            boolean b1 = false;
            boolean b2 = false;
            boolean b3 = false;

            if (player.hasPermission(permission)) {
                b1 = true;
            }

            if (item != null) {
                if (item.getDurability() == 0) {
                    String perm1 = permission + "." + item.getType().toString();
                    String perm2 = permission + "." + item.getType().toString() + ".0";

                    if (player.hasPermission(perm1) || player.hasPermission(perm2)) {
                        b2 = true;
                    }
                }

                if (player.hasPermission(permission + "." + item.getType().toString() + "." + item.getDurability())) {
                    b3 = true;
                }
            }

            if (!(b1 || b2 || b3)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Get a set for the location(s) of the shop's chest(s)
     * @param shop The shop
     * @return A set of 1 or 2 locations
     */
    public static Set<Location> getChestLocations(Shop shop) {
        Set<Location> chestLocations = new HashSet<>();
        InventoryHolder ih = shop.getInventoryHolder();
        if (ih instanceof DoubleChest) {
            DoubleChest dc = (DoubleChest) ih;
            chestLocations.add(((Chest) dc.getLeftSide()).getLocation());
            chestLocations.add(((Chest) dc.getRightSide()).getLocation());
        } else {
            chestLocations.add(shop.getLocation());
        }
        return chestLocations;
    }

    /**
     * Send a clickable update notification to the given player.
     * @param plugin An instance of the {@link ShopChest} plugin
     * @param p The player to receive the notification
     */
    public static void sendUpdateMessage(ShopChest plugin, Player p) {
        final MessageRegistry messageRegistry = plugin.getLanguageManager().getMessageRegistry();
        plugin.getPlatform().getTextComponentHelper().sendUpdateMessage(
                p,
                messageRegistry.getMessage(Message.UPDATE_AVAILABLE,
                        new Replacement(Placeholder.VERSION, plugin.getLatestVersion())),
                messageRegistry.getMessage(Message.UPDATE_CLICK_TO_DOWNLOAD),
                plugin.getDownloadLink()
        );
    }

    private final static int majorVersion;
    private final static int revision;

    static {
        String rawMajorVersion = null;
        try {
            final String bukkitVersion = Bukkit.getServer().getBukkitVersion();
            final String[] minecraftVersion = bukkitVersion.substring(0, bukkitVersion.indexOf('-')).split("\\.");
            rawMajorVersion = minecraftVersion[1];
        } catch (Exception e) {
            try {
                final String packageName = Bukkit.getServer().getClass().getPackage().getName();
                final String serverVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
                rawMajorVersion = serverVersion.split("_")[1];
            } catch (Exception ex) {
                if (rawMajorVersion == null) {
                    throw new RuntimeException("Could not load major version");
                }
            }
        }
        int parsedMajorVersion = -1;
        try {
            parsedMajorVersion = Integer.valueOf(rawMajorVersion);
        } catch (Exception e) {
            throw new RuntimeException("Could not parse major version");
        }
        int parsedRevision = 0;
        if (parsedMajorVersion < 17) {
            try {
                final String packageName = Bukkit.getServer().getClass().getPackage().getName();
                final String serverVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
                final String rawRevision = serverVersion.substring(serverVersion.length() - 1);
                parsedRevision = Integer.valueOf(rawRevision);
            } catch (Exception e) {}
        }
        majorVersion = parsedMajorVersion;
        revision = parsedRevision;
    }

    /**
     * @return The current server version with revision number (e.g. v1_9_R2, v1_10_R1)
     */
    private static String getServerVersion() {
        /*
        String packageName = Bukkit.getServer().getClass().getPackage().getName();

        return packageName.substring(packageName.lastIndexOf('.') + 1);
        */
        return "";
    }

    /**
     * @return The revision of the current server version (e.g. <i>2</i> for v1_9_R2, <i>1</i> for v1_10_R1)
     */
    public static int getRevision() {
        return revision;
    }

    /**
     * @return The major version of the server (e.g. <i>9</i> for 1.9.2, <i>10</i> for 1.10)
     */
    public static int getMajorVersion() {
        return majorVersion;
    }

    /**
     * Encodes an {@link ItemStack} in a Base64 String
     * @param itemStack {@link ItemStack} to encode
     * @return Base64 encoded String
     */
    public static String encode(ItemStack itemStack) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("i", itemStack);
        return Base64.getEncoder().encodeToString(config.saveToString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes an {@link ItemStack} from a Base64 String
     * @param string Base64 encoded String to decode
     * @return Decoded {@link ItemStack}
     */
    public static ItemStack decode(String string) {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(new String(Base64.getDecoder().decode(string), StandardCharsets.UTF_8));
        } catch (IllegalArgumentException | InvalidConfigurationException e) {
            e.printStackTrace();
            return null;
        }
        return config.getItemStack("i", null);
    }


    public static Object formatTimestamp(long timestamp) {
        // Format the timestamp as dd.MM HH:mm
        Date date = new Date(timestamp);
        return String.format("%1$td.%1$tm %1$tH:%1$tM", date);
    }
}
