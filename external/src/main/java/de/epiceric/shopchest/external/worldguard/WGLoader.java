package de.epiceric.shopchest.external.worldguard;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.epiceric.shopchest.external.ExternalLoadData;
import de.epiceric.shopchest.external.ExternalLoader;
import de.epiceric.shopchest.hook.HookManager;
import org.bukkit.plugin.Plugin;

public class WGLoader extends ExternalLoader {

    // Maybe need to enable listener for deny message (see old listeners2)

    private WGFlagRegistry flagRegistry;

    public WGLoader(HookManager hookManager, ExternalLoadData loadData) {
        super(hookManager, loadData);
    }

    @Override
    public String getName() {
        return "WorldGuard";
    }

    @Override
    public void check() {
        // Get WorldGuard instance
        final Plugin plugin = getPluginInstance(getName());

        // Check WorldGuard version and type
        final String wgVersion = plugin.getDescription().getVersion();
        final String majorVersion = wgVersion.split("\\.")[0];
        // TODO Add v6 compatibility (maybe)
        if (!"7".equals(majorVersion)) {
            throw new IllegalArgumentException("Unsupported " + getName() + " version : " + wgVersion);
        }
        checkPluginInstanceType(plugin, WorldGuardPlugin.class);
    }

    @Override
    public void load() {
        // Register flags
        if (flagRegistry == null) {
            flagRegistry = new WGFlagRegistry();
        }
        flagRegistry.initialize(loadData);
        flagRegistry.register();
    }

    @Override
    public void enable() {
        // Register hooks
        final WGCreateHook createHook = new WGCreateHook(flagRegistry);
        hookManager.registerCreateShopHook(createHook);
        hookManager.registerExtendShopHook(createHook);
        final WGUseHook useHook = new WGUseHook(flagRegistry);
        hookManager.registerUseShopHook(useHook);
    }
}
