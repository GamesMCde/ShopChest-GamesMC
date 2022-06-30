package de.epiceric.shopchest;

import de.epiceric.shopchest.command.ShopCommand;
import de.epiceric.shopchest.config.Config;
import de.epiceric.shopchest.config.hologram.HologramFormat;
import de.epiceric.shopchest.debug.DebugLogger;
import de.epiceric.shopchest.debug.NullDebugLogger;
import de.epiceric.shopchest.event.ShopInitializedEvent;
import de.epiceric.shopchest.external.ExternalManager;
import de.epiceric.shopchest.hook.HookManager;
import de.epiceric.shopchest.language.LanguageUtils;
import de.epiceric.shopchest.listeners.*;
import de.epiceric.shopchest.nms.Platform;
import de.epiceric.shopchest.nms.PlatformLoader;
import de.epiceric.shopchest.sql.Database;
import de.epiceric.shopchest.sql.MySQL;
import de.epiceric.shopchest.sql.SQLite;
import de.epiceric.shopchest.utils.*;
import de.epiceric.shopchest.utils.UpdateChecker.UpdateCheckerResult;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class ShopChest extends JavaPlugin {

    private static ShopChest instance;

    private Config config;
    private Platform platform;
    private HologramFormat hologramFormat;
    private ShopCommand shopCommand;
    private Economy econ = null;
    private Database database;
    private boolean isUpdateNeeded = false;
    private String latestVersion = "";
    private String downloadLink = "";
    private ShopUtils shopUtils;
    private DebugLogger debugLogger;
    private HookManager hookManager;
    private ExternalManager externalManager;
    private ShopUpdater updater;
    private ExecutorService shopCreationThreadPool;

    /**
     * @return An instance of ShopChest
     */
    public static ShopChest getInstance() {
        return instance;
    }

    /**
     * Sets up the economy of Vault
     * @return Whether an economy plugin has been registered
     */
    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public void onLoad() {
        instance = this;

        // Load the config
        config = new Config(this);

        // Load the debugger
        debugLogger = Config.enableDebugLog ?
                DebugLogger.getLogger(new File(getDataFolder(), "debug.txt"), getLogger())
                : new NullDebugLogger(getLogger());

        debugLogger.debug("Loading ShopChest version " + getDescription().getVersion());

        // Load external integrations
        hookManager = new HookManager();
        externalManager = new ExternalManager(hookManager, debugLogger);

        externalManager.setup();
        externalManager.check();
        externalManager.load();
    }

    @Override
    public void onEnable() {
        debugLogger.debug("Enabling ShopChest version " + getDescription().getVersion());

        // Load Vault
        // TODO Load Vault in dedicated class
        if (!getServer().getPluginManager().isPluginEnabled("Vault")) {
            debugLogger.debug("Could not find plugin \"Vault\"");
            getLogger().severe("Could not find plugin \"Vault\"");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!setupEconomy()) {
            debugLogger.debug("Could not find any Vault economy dependency!");
            getLogger().severe("Could not find any Vault economy dependency!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Load NMS
        final PlatformLoader platformLoader = new PlatformLoader(debugLogger);
        try {
            platform = platformLoader.loadPlatform();
        } catch (RuntimeException e) {
            debugLogger.debug(e.getMessage());
            debugLogger.debug("Disabling the plugin");
            debugLogger.getLogger().warning(e.getMessage());
            debugLogger.getLogger().warning("Disabling the plugin");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Load shop utils
        shopUtils = new ShopUtils(this);

        // Load language messages
        saveResource("item_names.txt", true);
        LanguageUtils.load();

        // Load hologram format
        File hologramFormatFile = new File(getDataFolder(), "hologram-format.yml");
        if (!hologramFormatFile.exists()) {
            saveResource("hologram-format.yml", false);
        }
        hologramFormat = new HologramFormat(this);
        hologramFormat.load();

        // Load commands
        shopCommand = new ShopCommand(this);

        shopCreationThreadPool = new ThreadPoolExecutor(0, 8,
                5L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        initDatabase();
        checkForUpdates();
        registerListeners();
        externalManager.enable();
        initializeShops();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        updater = new ShopUpdater(this);
        updater.start();
    }

    @Override
    public void onDisable() {
        debugLogger.debug("Disabling ShopChest...");

        if (shopUtils == null) {
            // Plugin has not been fully enabled (probably due to errors),
            // so only close file writer.
            debugLogger.close();
            return;
        }

        if (getShopCommand() != null) {
            getShopCommand().unregister();
        }

        ClickType.clear();

        if (updater != null) {
            debugLogger.debug("Stopping updater");
            updater.stop();
        }

        if (shopCreationThreadPool != null) {
            shopCreationThreadPool.shutdown();
        }

        shopUtils.removeShops();
        debugLogger.debug("Removed shops");

        if (database != null && database.isInitialized()) {
            if (database instanceof SQLite) {
                ((SQLite) database).vacuum();
            }

            database.disconnect();
        }

        debugLogger.close();
    }

    private void initDatabase() {
        if (Config.databaseType == Database.DatabaseType.SQLite) {
            debugLogger.debug("Using database type: SQLite");
            getLogger().info("Using SQLite");
            database = new SQLite(this);
        } else {
            debugLogger.debug("Using database type: MySQL");
            getLogger().info("Using MySQL");
            database = new MySQL(this);
            if (Config.databaseMySqlPingInterval > 0) {
                Bukkit.getScheduler().runTaskTimer(this, () -> {
                    if (database instanceof MySQL) {
                        ((MySQL) database).ping();
                    }
                }, Config.databaseMySqlPingInterval * 20L, Config.databaseMySqlPingInterval * 20L);
            }
        }
    }

    private void checkForUpdates() {
        if (!Config.enableUpdateChecker) {
            return;
        }
        
        new BukkitRunnable() {
            @Override
            public void run() {
                UpdateChecker uc = new UpdateChecker(ShopChest.this);
                UpdateCheckerResult result = uc.check();

                switch (result) {
                    case TRUE:
                        latestVersion = uc.getVersion();
                        downloadLink = uc.getLink();
                        isUpdateNeeded = true;

                        getLogger().warning(String.format("Version %s is available! You are running version %s.",
                                latestVersion, getDescription().getVersion()));

                        for (Player p : getServer().getOnlinePlayers()) {
                            if (p.hasPermission(Permissions.UPDATE_NOTIFICATION)) {
                                Utils.sendUpdateMessage(ShopChest.this, p);
                            }
                        }
                        break;
                
                    case FALSE:
                        latestVersion = "";
                        downloadLink = "";
                        isUpdateNeeded = false;
                        break;

                    case ERROR:
                        latestVersion = "";
                        downloadLink = "";
                        isUpdateNeeded = false;
                        getLogger().severe("An error occurred while checking for updates.");
                        break;
                }
            }
        }.runTaskAsynchronously(this);
    }

    private void registerListeners() {
        debugLogger.debug("Registering listeners...");
        getServer().getPluginManager().registerEvents(new ShopUpdateListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopItemListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new NotifyPlayerOnJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new ChestProtectListener(this), this);
        getServer().getPluginManager().registerEvents(new CreativeModeListener(this), this);

        if (!Utils.getServerVersion().equals("v1_8_R1")) {
            getServer().getPluginManager().registerEvents(new BlockExplodeListener(this), this);
        }
    }

    /**
     * Initializes the shops
     */
    private void initializeShops() {
        getShopDatabase().connect(new Callback<Integer>(this) {
            @Override
            public void onResult(Integer result) {
                Chunk[] loadedChunks = getServer().getWorlds().stream().map(World::getLoadedChunks)
                        .flatMap(Stream::of).toArray(Chunk[]::new);

                shopUtils.loadShopAmounts(new Callback<Map<UUID,Integer>>(ShopChest.this) {
                    @Override
                    public void onResult(Map<UUID, Integer> result) {
                        getLogger().info("Loaded shop amounts");
                        debugLogger.debug("Loaded shop amounts");
                    }
                    
                    @Override
                    public void onError(Throwable throwable) {
                        getLogger().severe("Failed to load shop amounts. Shop limits will not be working correctly!");
                        if (throwable != null) getLogger().severe(throwable.getMessage());
                    }
                });

                shopUtils.loadShops(loadedChunks, new Callback<Integer>(ShopChest.this) {
                    @Override
                    public void onResult(Integer result) {
                        getServer().getPluginManager().callEvent(new ShopInitializedEvent(result));
                        getLogger().info("Loaded " + result + " shops in already loaded chunks");
                        debugLogger.debug("Loaded " + result + " shops in already loaded chunks");
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        getLogger().severe("Failed to load shops in already loaded chunks");
                        if (throwable != null) getLogger().severe(throwable.getMessage());
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                // Database connection probably failed => disable plugin to prevent more errors
                getLogger().severe("No database access. Disabling ShopChest");
                if (throwable != null) getLogger().severe(throwable.getMessage());
                getServer().getPluginManager().disablePlugin(ShopChest.this);
            }
        });
    }

    /**
     * @return The {@link DebugLogger} instance
     */
    public DebugLogger getDebugLogger() {
        return debugLogger;
    }

    /**
     * @return A thread pool for executing shop creation tasks
     */
    public ExecutorService getShopCreationThreadPool() {
        return shopCreationThreadPool;
    }

    public Platform getPlatform() {
        return platform;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public HologramFormat getHologramFormat() {
        return hologramFormat;
    }

    public ShopCommand getShopCommand() {
        return shopCommand;
    }

    /**
     * @return The {@link ShopUpdater} that schedules hologram and item updates
     */
    public ShopUpdater getUpdater() {
        return updater;
    }

    /**
     * @return ShopChest's {@link ShopUtils} containing some important methods
     */
    public ShopUtils getShopUtils() {
        return shopUtils;
    }

    /**
     * @return Registered Economy of Vault
     */
    public Economy getEconomy() {
        return econ;
    }

    /**
     * @return ShopChest's shop database
     */
    public Database getShopDatabase() {
        return database;
    }

    /**
     * @return Whether an update is needed (will return false if not checked)
     */
    public boolean isUpdateNeeded() {
        return isUpdateNeeded;
    }

    /**
     * Set whether an update is needed
     * @param isUpdateNeeded Whether an update should be needed
     */
    public void setUpdateNeeded(boolean isUpdateNeeded) {
        this.isUpdateNeeded = isUpdateNeeded;
    }

    /**
     * @return The latest version of ShopChest (will return null if not checked or if no update is available)
     */
    public String getLatestVersion() {
        return latestVersion;
    }

    /**
     * Set the latest version
     * @param latestVersion Version to set as latest version
     */
    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    /**
     * @return The download link of the latest version (will return null if not checked or if no update is available)
     */
    public String getDownloadLink() {
        return downloadLink;
    }

    /**
     * Set the download Link of the latest version (will return null if not checked or if no update is available)
     * @param downloadLink Link to set as Download Link
     */
    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    /**
     * @return The {@link Config} of ShopChest
     */
    public Config getShopChestConfig() {
        return config;
    }
}
