package me.st28.flexseries.flexcore.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Represents an external plugin hook.
 */
public abstract class Hook {

    private final String pluginName;

    public Hook(String pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * @return the name of the plugin.
     */
    public final String getPluginName() {
        return pluginName;
    }

    public final Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin(getPluginName());
    }

    public final void enable() {
        handleEnable();
    }

    protected abstract void handleEnable();

    public final void disable() {
        handleDisable();
    }

    protected abstract void handleDisable();

}