package me.st28.flexseries.flexcore.logging;

import me.st28.flexseries.flexcore.plugin.module.FlexModule;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class LogHelper {

    private LogHelper() { }

    private static JavaPlugin getPlugin(Class<? extends JavaPlugin> clazz) {
        JavaPlugin plugin = JavaPlugin.getPlugin(clazz);
        if (plugin == null) {
            throw new IllegalStateException("Unable to log message: No plugin with class '" + clazz.getCanonicalName() + "' is loaded.");
        }
        return plugin;
    }

    public static void debug(Class<? extends JavaPlugin> pluginClass, String message) {
        LogHelper.debug(JavaPlugin.getPlugin(pluginClass), message);
    }

    public static void debug(JavaPlugin plugin, String message) {
        if (plugin.getConfig().getBoolean("debug")) {
            Bukkit.getLogger().log(Level.INFO, String.format("[%s DEBUG] %s", plugin.getName(), message));
        }
    }

    public static void debug(FlexModule module, String message) {
        if (module.getPlugin().getConfig().getBoolean("debug")) {
            Bukkit.getLogger().log(Level.INFO, String.format("[%s/%s DEBUG] %s", module.getPlugin().getName(), module.getIdentifier(), message));
        }
    }

    public static void info(Class<? extends JavaPlugin> pluginClass, String message) {
        LogHelper.info(JavaPlugin.getPlugin(pluginClass), message);
    }

    public static void info(JavaPlugin plugin, String message) {
        Bukkit.getLogger().log(Level.INFO, String.format("[%s] %s", plugin.getName(), message));
    }

    public static void info(FlexModule module, String message) {
        Bukkit.getLogger().log(Level.INFO, String.format("[%s/%s] %s", module.getPlugin().getName(), module.getIdentifier(), message));
    }

    public static void warning(Class<? extends JavaPlugin> pluginClass, String message) {
        LogHelper.warning(JavaPlugin.getPlugin(pluginClass), message);
    }

    public static void warning(JavaPlugin plugin, String message) {
        Bukkit.getLogger().log(Level.WARNING, String.format("[%s] %s", plugin.getName(), message));
    }

    public static void warning(FlexModule module, String message) {
        Bukkit.getLogger().log(Level.WARNING, String.format("[%s/%s] %s", module.getPlugin().getName(), module.getIdentifier(), message));
    }

    public static void severe(Class<? extends JavaPlugin> pluginClass, String message) {
        LogHelper.severe(JavaPlugin.getPlugin(pluginClass), message);
    }

    public static void severe(JavaPlugin plugin, String message) {
        Bukkit.getLogger().log(Level.SEVERE, String.format("[%s] %s", plugin.getName(), message));
    }

    public static void severe(FlexModule module, String message) {
        Bukkit.getLogger().log(Level.SEVERE, String.format("[%s/%s] %s", module.getPlugin().getName(), module.getIdentifier(), message));
    }

}