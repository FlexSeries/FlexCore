package me.st28.flexseries.flexcore.players;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Tracks the name and display name associated with the UUID of each player that has joined the server.
 */
public final class PlayerUUIDTracker extends FlexModule<FlexCore> implements Listener {

    private YamlFileManager uuidFile;

    private final Map<UUID, String> uuidsToNames = new HashMap<>();
    private final Map<String, UUID> namesToUuids = new HashMap<>();
    private final Map<UUID, String> uuidsToDisplayNames = new HashMap<>();
    private final Map<String, UUID> displayNamesToUuids = new HashMap<>();

    public PlayerUUIDTracker(FlexCore plugin) {
        super(plugin, "uuid_tracker", "Tracks the UUIDs, names, and display names of joined players", PlayerManager.class);
    }

    @Override
    public void handleLoad() throws Exception {
        uuidFile = new YamlFileManager(plugin.getDataFolder() + File.separator + "playerUuids.yml");

        FileConfiguration config = uuidFile.getConfig();
        for (String rawUuid : config.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(rawUuid);
            } catch (Exception ex) {
                plugin.getLogger().severe("Unable to load UUID '" + rawUuid + "' from playerUuids.yml for plugin: '" + plugin.getName() + "'");
                continue;
            }

            String name = config.getString(rawUuid + ".name");
            if (name != null) {
                uuidsToNames.put(uuid, name);
                namesToUuids.put(name.toLowerCase(), uuid);
            }

            String displayName = config.getString(rawUuid + ".displayName");
            if (displayName != null) {
                uuidsToDisplayNames.put(uuid, ChatColor.translateAlternateColorCodes('&', displayName));
                namesToUuids.put(getFriendlyDisplayName(ChatColor.translateAlternateColorCodes('&', displayName)), uuid);
            }

            if (displayName == null) {
                LogHelper.debug(FlexCore.class, "Loaded UUID/name: " + uuid.toString() + "/" + name);
            } else {
                LogHelper.debug(FlexCore.class, "Loaded UUID/name/display name: " + uuid.toString() + " / " + name + " / " + displayName);
            }
        }
    }

    @Override
    public void handleSave(boolean async) {
        FileConfiguration config = uuidFile.getConfig();
        for (Entry<UUID, String> entry : uuidsToNames.entrySet()) {
            config.set(entry.getKey().toString() + ".name", entry.getValue());
        }

        for (Entry<UUID, String> entry : uuidsToDisplayNames.entrySet()) {
            config.set(entry.getKey().toString() + ".displayName", entry.getValue().replace(ChatColor.COLOR_CHAR, '&'));
        }
        uuidFile.save();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        String curDispName = uuidsToDisplayNames.get(e.getPlayer().getUniqueId());
        if (curDispName != null) {
            e.getPlayer().setDisplayName(curDispName);
        }

        updatePlayer(e.getPlayer());
    }

    /**
     * Updates a player's name and display name entries.
     *
     * @param p The player to update.
     */
    public void updatePlayer(Player p) {
        UUID uuid = p.getUniqueId();

        String name = p.getName();
        uuidsToNames.put(uuid, name);
        namesToUuids.put(name.toLowerCase(), uuid);

        try {
            String temp = uuidsToDisplayNames.remove(uuid);
            displayNamesToUuids.remove(getFriendlyDisplayName(temp));
        } catch (Exception ex) { }

        String displayName = p.getDisplayName();
        if (!displayName.equals(name)) {
            uuidsToDisplayNames.put(uuid, ChatColor.translateAlternateColorCodes('&', displayName));
            displayNamesToUuids.put(getFriendlyDisplayName(displayName), uuid);
        }
    }

    private String getFriendlyDisplayName(String displayName) {
        return ChatColor.stripColor(displayName).toLowerCase();
    }

    /**
     * @return The cached UUID for a given name or display name.
     */
    public UUID getUuid(String name, boolean isDisplayName) {
        Validate.notNull(name, "Name cannot be null.");
        return isDisplayName ? displayNamesToUuids.get(getFriendlyDisplayName(name)) : namesToUuids.get(name.toLowerCase());
    }

    /**
     * @return The cached name for a given UUID.
     */
    public String getName(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null.");
        return uuidsToNames.get(uuid);
    }

    /**
     * @return The cached display name for a given UUID.
     */
    public String getDisplayName(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null.");
        return uuidsToDisplayNames.get(uuid);
    }

    /**
     * Returns the name that should be displayed to players.<br />
     * <b>Order:</b>  display name, name, uuid
     *
     * @param uuid The UUID to get the name of.
     * @return The name that should be displayed to players.
     */
    public String getTopLevelName(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null.");

        if (uuidsToDisplayNames.containsKey(uuid)) {
            return uuidsToDisplayNames.get(uuid);
        }

        if (uuidsToNames.containsKey(uuid)) {
            return uuidsToNames.get(uuid);
        }

        return uuid.toString();
    }

    /**
     * @return an unmodifiable view of the display name to UUID index map.
     */
    public Map<String, UUID> getDisplayNamesToUuids() {
        return Collections.unmodifiableMap(displayNamesToUuids);
    }

    /**
     * @return an unmodifiable view of the name to UUID index map.
     */
    public Map<String, UUID> getNamesToUuids() {
        return Collections.unmodifiableMap(namesToUuids);
    }

}