package me.st28.flexseries.flexcore.player.uuid_tracker;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.events.PlayerJoinLoadedEvent;
import me.st28.flexseries.flexcore.player.PlayerManager;
import me.st28.flexseries.flexcore.plugin.module.FlexModule;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Tracks the name associated with the UUID of each player that has joined the server.
 */
public final class PlayerUuidTracker extends FlexModule<FlexCore> implements Listener {

    private UuidTrackerStorageHandler storageHandler;

    private final Map<UUID, String> uuidsToNames = new HashMap<>();
    private final Map<String, UUID> namesToUuids = new HashMap<>();

    public PlayerUuidTracker(FlexCore plugin) {
        super(plugin, "uuid_tracker", "Name to UUID and vice versa indexes for players that join the server", false, PlayerManager.class);
    }

    @Override
    public void handleLoad() {
        FileConfiguration config = getConfig();
        String storageType = config.getString("storage.type", "YAML").toUpperCase();
        switch (storageType) {
            case "YAML":
                storageHandler = new UuidTrackerStorageHandlerYaml(this);
                break;

            case "MYSQL":
                storageHandler = new UuidTrackerStorageHandlerMySql(this, config.getString("storage.database"), config.getString("storage.prefix"));
                break;

            default:
                throw new IllegalArgumentException("Invalid storage type: '" + storageType + "'");
        }

        uuidsToNames.putAll(storageHandler.loadIndex());
        for (Entry<UUID, String> entry : uuidsToNames.entrySet()) {
            namesToUuids.put(entry.getValue().toLowerCase(), entry.getKey());
        }
    }

    @Override
    public void handleSave(boolean async) {
        storageHandler.saveIndex(uuidsToNames);
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