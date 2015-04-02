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
package me.st28.flexseries.flexcore.cookie;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.events.PlayerLeaveEvent;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.player.PlayerManager;
import me.st28.flexseries.flexcore.player.loading.PlayerLoadCycle;
import me.st28.flexseries.flexcore.player.loading.PlayerLoader;
import me.st28.flexseries.flexcore.plugin.module.FlexModule;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Manages small per-user preferences, such as, but not limited to:
 * <ul>
 *     <li>Most frequently used command labels</li>
 * </ul>
 */
public final class CookieManager extends FlexModule<FlexCore> implements Listener, PlayerLoader {

    /**
     * @return the user identifier for a given CommandSender.
     */
    public static String getUserIdentifier(CommandSender sender) {
        Validate.notNull(sender, "Sender cannot be null.");

        if (sender instanceof ConsoleCommandSender) {
            return CONSOLE_IDENTIFIER;
        } else if (sender instanceof Player) {
            return ((Player) sender).getUniqueId().toString();
        }
        throw new UnsupportedOperationException("No user identifier available for class '" + sender.getClass().getCanonicalName() + "'");
    }

    public static final String CONSOLE_IDENTIFIER = "CONSOLE";

    private File cookieDir;

    private final Map<String, Map<String, String>> loadedCookies = new HashMap<>();

    public CookieManager(FlexCore plugin) {
        super(plugin, "cookies", "Manages small per-user preferences", true, PlayerManager.class);
    }

    @Override
    protected final void handleLoad() {
        cookieDir = new File(getDataFolder() + File.separator + "data");
        loadEntry(CONSOLE_IDENTIFIER);
    }

    @Override
    protected final void handleReload() {
        cookieDir.mkdir();
    }

    @Override
    protected final void handleSave(boolean async) {
        for (String entry : loadedCookies.keySet()) {
            saveEntry(entry);
        }
    }

    private void loadEntry(String entry) {
        Map<String, String> values = new HashMap<>();

        try {
            FileConfiguration config = new YamlFileManager(cookieDir + File.separator + entry + ".yml").getConfig();

            for (String key : config.getKeys(false)) {
                values.put(key, config.getString(key));
            }
        } catch (Exception ex) {
            LogHelper.warning(this, "An error occurred while loading entry '" + entry + "' - starting fresh.");
        }

        loadedCookies.put(entry, values);
    }

    private void saveEntry(String entry) {
        YamlFileManager file = new YamlFileManager(cookieDir + File.separator + entry + ".yml");
        FileConfiguration config = file.getConfig();

        for (Entry<String, String> subEntry : loadedCookies.get(entry).entrySet()) {
            config.set(subEntry.getKey(), subEntry.getValue());
        }

        file.save();
    }

    @EventHandler
    public void onPlayerLeave(PlayerLeaveEvent e) {
        String identifier = e.getPlayer().getUniqueId().toString();

        saveEntry(identifier);
        loadedCookies.remove(identifier);
    }

    /**
     * @see #getValue(String, String, Class, String)
     */
    public String getValue(String userId, Class<? extends FlexPlugin> plugin, String identifier) {
        return getValue(userId, null, plugin, identifier);
    }

    /**
     * Retrieves a cookie value for a player.
     *
     * @param userId The ID of the user to retrieve the value of.
     * @param defaultValue The value to return if the cookie value is null.
     * @param plugin The plugin that owns the cookie.
     * @param identifier The identifier of the cookie.
     * @return The value of the cookie.<br />
     *         Null if no value is set.
     */
    public String getValue(String userId, String defaultValue, Class<? extends FlexPlugin> plugin, String identifier) {
        Validate.notNull(userId, "User ID cannot be null.");
        Validate.notNull(plugin, "Plugin cannot be null.");
        Validate.notNull(identifier, "Identifier cannot be null.");

        Map<String, String> cookies = loadedCookies.get(userId);

        if (cookies == null || cookies.isEmpty()) {
            return defaultValue;
        }

        String value = cookies.get(plugin.getCanonicalName() + "-" + identifier);
        return value == null ? defaultValue : value;
    }

    /**
     * Sets a cookie value for a player.
     *
     * @param userId The ID of the user to set the value for.
     * @param value The value to set for the player.
     * @param plugin The plugin that owns the cookie.
     * @param identifier The identifier of the cookie (unique per-plugin).
     */
    public final void setValue(String userId, String value, Class<? extends FlexPlugin> plugin, String identifier) {
        Validate.notNull(userId, "User ID cannot be null.");
        Validate.notNull(plugin, "Plugin cannot be null.");
        Validate.notNull(identifier, "Identifier cannot be null.");

        Map<String, String> cookies = loadedCookies.get(userId);
        if (cookies == null) {
            loadedCookies.put(userId, cookies = new HashMap<>());
        }

        cookies.put(plugin.getCanonicalName() + "-" + identifier, value);
    }

    @Override
    public boolean isPlayerLoadSync() {
        return false;
    }

    @Override
    public boolean loadPlayer(UUID uuid, String name, PlayerLoadCycle cycle) {
        loadEntry(uuid.toString());
        PlayerLoadCycle.completedCycle(cycle, this);
        return true;
    }

}