package de.epiceric.shopchest.external;

import de.epiceric.shopchest.hook.HookManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class ExternalLoader {

    protected final HookManager hookManager;
    protected final ExternalLoadData loadData;
    private boolean stopped;

    public ExternalLoader(HookManager hookManager, ExternalLoadData loadData) {
        this.hookManager = hookManager;
        this.loadData = loadData;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    protected Plugin getPluginInstance(String pluginName) {
        final Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (plugin == null) {
            throw new IllegalStateException("'" + pluginName + "' plugin is not present");
        }
        return plugin;
    }

    protected <T extends JavaPlugin> T checkPluginInstanceType(Plugin plugin, Class<T> clazz) {
        if (!clazz.isInstance(plugin)) {
            throw new IllegalArgumentException("Wrong '" + plugin.getName() + "' plugin");
        }
        return clazz.cast(plugin);
    }

    public abstract String getName();

    public abstract void check();

    public void load() {
    }

    public void enable() {
    }

}
