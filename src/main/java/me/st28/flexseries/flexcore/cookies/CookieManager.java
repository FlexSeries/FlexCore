package me.st28.flexseries.flexcore.cookies;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.events.PlayerLeaveEvent;
import me.st28.flexseries.flexcore.players.PlayerManager;
import me.st28.flexseries.flexcore.players.loading.PlayerLoadCycle;
import me.st28.flexseries.flexcore.players.loading.PlayerLoader;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
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
//TODO: Make plugin function without the cookie manager being enabled.
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
        FileConfiguration config = new YamlFileManager(cookieDir + File.separator + entry + ".yml").getConfig();

        //TODO: Wipe file is there's an error with the YAML syntax.

        Map<String, String> values = new HashMap<>();
        for (String key : config.getKeys(false)) {
            values.put(key, config.getString(key));
        }

        loadedCookies.put(entry, values);
    }

    private void saveEntry(String entry) {
        YamlFileManager file = new YamlFileManager(cookieDir + File.separator + entry + ".yml");
        FileConfiguration config = file.getConfig();

        for (Entry<String, String> subEntry : loadedCookies.get(identifier).entrySet()) {
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