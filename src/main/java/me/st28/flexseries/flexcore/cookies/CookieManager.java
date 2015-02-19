package me.st28.flexseries.flexcore.cookies;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.events.PlayerLeaveEvent;
import me.st28.flexseries.flexcore.players.PlayerManager;
import me.st28.flexseries.flexcore.players.loading.PlayerLoadCycle;
import me.st28.flexseries.flexcore.players.loading.PlayerLoader;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
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
//TODO: Unload on player leave, load on player join
public final class CookieManager extends FlexModule<FlexCore> implements Listener, PlayerLoader {

    private File cookieDir;

    private final Map<UUID, Map<String, String>> loadedCookies = new HashMap<>();

    public CookieManager(FlexCore plugin) {
        super(plugin, "cookies", "Manages small per-user preferences", PlayerManager.class);
    }

    @Override
    protected final void handleLoad() throws Exception {
        cookieDir = new File(plugin.getDataFolder() + File.separator + "cookies");
    }

    @Override
    protected final void handleReload() {
        cookieDir.mkdir();

        /*loadedCookies.clear();

        for (File file : cookieDir.listFiles()) {
            if (YamlFileManager.YAML_FILE_PATTERN.matcher(file.getName()).matches()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(file.getName().replace(".yml", ""));
                } catch (Exception ex) {
                    if (!file.getName().equals("console.yml")) {
                        LogHelper.warning(this, "Invalid UUID in filename: " + file.getName());
                        continue;
                    }

                    uuid = null;
                }

                FileConfiguration config = new YamlFileManager(file).getConfig();

                Map<String, String> values = new HashMap<>();
                for (String key : config.getKeys(false)) {
                    values.put(key, config.getString(key));
                }

                loadedCookies.put(uuid, values);
            }
        }*/
    }

    @Override
    protected final void handleSave(boolean async) {
        for (UUID entry : loadedCookies.keySet()) {
            saveEntry(entry);
        }
    }

    private void loadEntry(UUID entry) {
        FileConfiguration config = new YamlFileManager(cookieDir + File.separator + (identifier == null ? "console" : entry.toString()) + ".yml").getConfig();

        //TODO: Wipe file is there's an error with the YAML syntax.

        Map<String, String> values = new HashMap<>();
        for (String key : config.getKeys(false)) {
            values.put(key, config.getString(key));
        }

        loadedCookies.put(entry, values);
    }

    private void saveEntry(UUID entry) {
        UUID identifier = entry == null ? null : entry;

        YamlFileManager file = new YamlFileManager(cookieDir + File.separator + (identifier == null ? "console" : entry.toString()) + ".yml");
        FileConfiguration config = file.getConfig();

        for (Entry<String, String> subEntry : loadedCookies.get(identifier).entrySet()) {
            config.set(subEntry.getKey(), subEntry.getValue());
        }

        file.save();
    }

    @EventHandler
    public void onPlayerQuit(PlayerLeaveEvent e) {
        saveEntry(e.getPlayer().getUniqueId());
        loadedCookies.remove(e.getPlayer().getUniqueId());
    }

    /**
     * @see #getValue(org.bukkit.entity.Player, String, Class, String)
     */
    public String getValue(Player player, Class<? extends FlexPlugin> plugin, String identifier) {
        return getValue(player, null, plugin, identifier);
    }

    /**
     * Retrieves a cookie value for a player.
     *
     * @param player the player to retrieve the value of.<br />
     *               If null, will return the value for the console.
     * @param defaultValue The value to return if the cookie value is null.
     * @param plugin The plugin that owns the cookie.
     * @param identifier The identifier of the cookie (unique per-plugin).
     * @return The value of the cookie.<br />
     *         Null if no value is set.
     */
    public String getValue(Player player, String defaultValue, Class<? extends FlexPlugin> plugin, String identifier) {
        Map<String, String> cookies = loadedCookies.get(player == null ? null : player.getUniqueId());

        if (cookies == null || cookies.isEmpty()) {
            return defaultValue;
        }

        String value = cookies.get(plugin.getCanonicalName() + "-" + identifier);
        return value == null ? defaultValue : value;
    }

    /**
     * Sets a cookie value for a player.
     *
     * @param player The player to set the value for.<br />
     *               If null, sets the console's value.
     * @param value The value to set for the player.
     * @param plugin The plugin that owns the cookie.
     * @param identifier The identifier of the cookie (unique per-plugin).
     */
    public final void setValue(Player player, String value, Class<? extends FlexPlugin> plugin, String identifier) {
        Map<String, String> cookies = loadedCookies.get(player == null ? null : player.getUniqueId());
        if (cookies == null) {
            loadedCookies.put(player == null ? null : player.getUniqueId(), cookies = new HashMap<>());
        }

        cookies.put(plugin.getCanonicalName() + "-" + identifier, value);
    }

    @Override
    public boolean isPlayerLoadSync() {
        return false;
    }

    @Override
    public boolean loadPlayer(UUID uuid, String name, PlayerLoadCycle cycle) {
        loadEntry(uuid);
        PlayerLoadCycle.completedCycle(cycle, this);
        return true;
    }

}