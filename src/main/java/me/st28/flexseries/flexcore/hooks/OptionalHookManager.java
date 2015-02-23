package me.st28.flexseries.flexcore.hooks;

import org.apache.commons.lang.Validate;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class OptionalHookManager {

    private JavaPlugin plugin;
    private Class<? extends Hook> hookClass;

    public OptionalHookManager(JavaPlugin plugin, Class<? extends Hook> hookClass) {
        Validate.notNull(plugin, "Plugin cannot be null.");
        Validate.notNull(plugin, "Hook class cannot be null.");

        this.plugin = plugin;
        this.hookClass = hookClass;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public Class<? extends Hook> getHookClass() {
        return hookClass;
    }

    public final void enable() {
        handleEnable();
    }

    protected void handleEnable() { }

    public final void disable() {
        handleDisable();
        plugin = null;
        hookClass = null;
    }

    protected void handleDisable() { }

}