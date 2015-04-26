/**
 * FlexCore - Licensed under the MIT License (MIT)
 *
 * Copyright (c) Stealth2800 <http://stealthyone.com/>
 * Copyright (c) contributors <https://github.com/FlexSeries>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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

    public static void debug(Class<? extends JavaPlugin> pluginClass, String message, Throwable throwable) {
        LogHelper.debug(JavaPlugin.getPlugin(pluginClass), message, throwable);
    }

    public static void debug(JavaPlugin plugin, String message) {
        if (plugin.getConfig().getBoolean("debug")) {
            Bukkit.getLogger().log(Level.INFO, String.format("[%s DEBUG] %s", plugin.getName(), message));
        }
    }

    public static void debug(JavaPlugin plugin, String message, Throwable throwable) {
        if (plugin.getConfig().getBoolean("debug")) {
            Bukkit.getLogger().log(Level.INFO, String.format("[%s DEBUG] %s", plugin.getName(), message), throwable);
        }
    }

    public static void debug(FlexModule module, String message) {
        if (module.getPlugin().getConfig().getBoolean("debug")) {
            Bukkit.getLogger().log(Level.INFO, String.format("[%s/%s DEBUG] %s", module.getPlugin().getName(), module.getIdentifier(), message));
        }
    }

    public static void debug(FlexModule module, String message, Throwable throwable) {
        if (module.getPlugin().getConfig().getBoolean("debug")) {
            Bukkit.getLogger().log(Level.INFO, String.format("[%s/%s DEBUG] %s", module.getPlugin().getName(), module.getIdentifier(), message), throwable);
        }
    }

    public static void info(Class<? extends JavaPlugin> pluginClass, String message) {
        LogHelper.info(JavaPlugin.getPlugin(pluginClass), message);
    }

    public static void info(Class<? extends JavaPlugin> pluginClass, String message, Throwable throwable) {
        LogHelper.info(JavaPlugin.getPlugin(pluginClass), message, throwable);
    }

    public static void info(JavaPlugin plugin, String message) {
        Bukkit.getLogger().log(Level.INFO, String.format("[%s] %s", plugin.getName(), message));
    }

    public static void info(JavaPlugin plugin, String message, Throwable throwable) {
        Bukkit.getLogger().log(Level.INFO, String.format("[%s] %s", plugin.getName(), message), throwable);
    }

    public static void info(FlexModule module, String message) {
        Bukkit.getLogger().log(Level.INFO, String.format("[%s/%s] %s", module.getPlugin().getName(), module.getIdentifier(), message));
    }

    public static void info(FlexModule module, String message, Throwable throwable) {
        Bukkit.getLogger().log(Level.INFO, String.format("[%s/%s] %s", module.getPlugin().getName(), module.getIdentifier(), message), throwable);
    }

    public static void warning(Class<? extends JavaPlugin> pluginClass, String message) {
        LogHelper.warning(JavaPlugin.getPlugin(pluginClass), message);
    }

    public static void warning(Class<? extends JavaPlugin> pluginClass, String message, Throwable throwable) {
        LogHelper.warning(JavaPlugin.getPlugin(pluginClass), message, throwable);
    }

    public static void warning(JavaPlugin plugin, String message) {
        Bukkit.getLogger().log(Level.WARNING, String.format("[%s] %s", plugin.getName(), message));
    }

    public static void warning(JavaPlugin plugin, String message, Throwable throwable) {
        Bukkit.getLogger().log(Level.WARNING, String.format("[%s] %s", plugin.getName(), message), throwable);
    }

    public static void warning(FlexModule module, String message) {
        Bukkit.getLogger().log(Level.WARNING, String.format("[%s/%s] %s", module.getPlugin().getName(), module.getIdentifier(), message));
    }

    public static void warning(FlexModule module, String message, Throwable throwable) {
        Bukkit.getLogger().log(Level.WARNING, String.format("[%s/%s] %s", module.getPlugin().getName(), module.getIdentifier(), message), throwable);
    }

    public static void severe(Class<? extends JavaPlugin> pluginClass, String message) {
        LogHelper.severe(JavaPlugin.getPlugin(pluginClass), message);
    }

    public static void severe(Class<? extends JavaPlugin> pluginClass, String message, Throwable throwable) {
        LogHelper.severe(JavaPlugin.getPlugin(pluginClass), message, throwable);
    }

    public static void severe(JavaPlugin plugin, String message) {
        Bukkit.getLogger().log(Level.SEVERE, String.format("[%s] %s", plugin.getName(), message));
    }

    public static void severe(JavaPlugin plugin, String message, Throwable throwable) {
        Bukkit.getLogger().log(Level.SEVERE, String.format("[%s] %s", plugin.getName(), message), throwable);
    }

    public static void severe(FlexModule module, String message) {
        Bukkit.getLogger().log(Level.SEVERE, String.format("[%s/%s] %s", module.getPlugin().getName(), module.getIdentifier(), message));
    }

    public static void severe(FlexModule module, String message, Throwable throwable) {
        Bukkit.getLogger().log(Level.SEVERE, String.format("[%s/%s] %s", module.getPlugin().getName(), module.getIdentifier(), message), throwable);
    }

}