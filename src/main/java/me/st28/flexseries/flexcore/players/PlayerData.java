package me.st28.flexseries.flexcore.players;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a loaded player's data.
 */
public final class PlayerData {

    private final UUID uuid;
    private final Map<String, Object> customData = new HashMap<>();

    Timestamp firstJoin;
    Timestamp lastLogin;
    Timestamp lastLogout;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the UUID of the player.
     */
    public UUID getUniqueId() {
        return uuid;
    }

    /**
     * @return the time of the player's first login to the server.
     */
    public Timestamp getFirstJoin() {
        return firstJoin;
    }

    /**
     * @return the time of the player's most recent login to the server.
     */
    public Timestamp getLastLogin() {
        return lastLogin;
    }

    /**
     * @return the time of the player's most recent logout from the server.
     */
    public Timestamp getLastLogout() {
        return lastLogout;
    }

    /**
     * Returns a custom data entry for the player.
     *
     * @param key The key for the data.
     * @param type The type of data the key represents.
     * @return A custom data entry matching the key.<br />
     *         Null if no entry was found with the given key.
     */
    public <T> T getCustomDataEntry(String key, T type) {
        return (T) customData.get(key);
    }

    /**
     * Sets a custom data value.
     *
     * @param key The key for the data.
     * @param obj The data to set. Should be configuration serializable.
     */
    public void setCustomDataEntry(String key, Object obj) {
        customData.put(key, obj);
    }

}