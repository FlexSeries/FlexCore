package me.st28.flexseries.flexcore.players;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.events.PlayerJoinLoadedEvent;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Tracks the name associated with the UUID of each player that has joined the server.
 */
public final class PlayerUUIDTracker extends FlexModule<FlexCore> implements Listener {

    private YamlFileManager uuidFile;

    private final Map<UUID, String> uuidsToNames = new HashMap<>();
    private final Map<String, UUID> namesToUuids = new HashMap<>();

    public PlayerUUIDTracker(FlexCore plugin) {
        super(plugin, "uuid_tracker", "Name to UUID and vice versa indexes for players that join the server", false, PlayerManager.class);
    }

    @Override
    public void handleLoad() {
        uuidFile = new YamlFileManager(FlexPlugin.getRegisteredModule(PlayerManager.class).getDataFolder() + File.separator + "uuidIndex.yml");

        FileConfiguration config = uuidFile.getConfig();
        for (String rawUuid : config.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(rawUuid);
            } catch (Exception ex) {
                plugin.getLogger().severe("Unable to load UUID '" + rawUuid + "' from uuidIndex.yml for plugin: '" + plugin.getName() + "'");
                continue;
            }

            String name = config.getString(rawUuid + ".name");
            if (name != null) {
                uuidsToNames.put(uuid, name);
                namesToUuids.put(name.toLowerCase(), uuid);
            }

            LogHelper.debug(FlexCore.class, "Loaded UUID/name: " + uuid.toString() + "/" + name);
        }
    }

    @Override
    public void handleSave(boolean async) {
        FileConfiguration config = uuidFile.getConfig();

        for (Entry<UUID, String> entry : uuidsToNames.entrySet()) {
            config.set(entry.getKey().toString() + ".name", entry.getValue());
        }

        uuidFile.save();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinLoaded(PlayerJoinLoadedEvent e) {
        updatePlayer(e.getPlayer());
    }

    /**
     * Updates a player's name entry.
     *
     * @param p The player to update.
     */
    public void updatePlayer(Player p) {
        UUID uuid = p.getUniqueId();

        String name = p.getName();
        uuidsToNames.put(uuid, name);
        namesToUuids.put(name.toLowerCase(), uuid);
    }

    /**
     * @return The cached UUID for a given name.
     */
    public UUID getUuid(String name) {
        Validate.notNull(name, "Name cannot be null.");
        return namesToUuids.get(name.toLowerCase());
    }

    /**
     * @return The cached name for a given UUID.
     */
    public String getName(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null.");
        return uuidsToNames.get(uuid);
    }

    /**
     * @return an unmodifiable view of the name to UUID index map.
     */
    public Map<String, UUID> getNamesToUuids() {
        return Collections.unmodifiableMap(namesToUuids);
    }

}