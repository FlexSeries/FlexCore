package me.st28.flexseries.flexcore.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for JavaPlugins
 */
public final class PluginUtils {

    private PluginUtils() { }

    private static Map<String, String> PLUGIN_NAMES = new HashMap<>();

    /**
     * @return The proper capitalization for a plugin's name, based on a raw name.
     */
    public static String getProperPluginName(String rawName) {
        if (PLUGIN_NAMES.containsKey(rawName.toLowerCase())) {
            return PLUGIN_NAMES.get(rawName.toLowerCase());
        }

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.getName().equalsIgnoreCase(rawName)) {
                String newName = plugin.getName();
                PLUGIN_NAMES.put(rawName.toLowerCase(), newName);
                return newName;
            }
        }
        return null;
    }

    /**
     * @return a plugin according to its raw name.
     */
    public static Plugin getPlugin(String rawName) {
        rawName = getProperPluginName(rawName);
        return rawName == null ? null : Bukkit.getPluginManager().getPlugin(rawName);
    }

}