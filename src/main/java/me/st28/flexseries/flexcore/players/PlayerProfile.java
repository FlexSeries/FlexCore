package me.st28.flexseries.flexcore.players;

import me.st28.flexseries.flexcore.players.uuid_tracker.PlayerUuidTracker;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import org.bukkit.ChatColor;

import java.util.UUID;

/**
 * A utility class for having a representation of a player and for easily retrieving the name and display name of a player.
 */
public final class PlayerProfile implements Comparable<PlayerProfile> {

    private final UUID uuid;
    private final String name;
    private final String displayName;

    public PlayerProfile(UUID uuid) {
        this.uuid = uuid;

        PlayerUuidTracker uuidTracker = FlexPlugin.getRegisteredModule(PlayerUuidTracker.class);
        name = uuidTracker.getName(uuid);
        displayName = uuidTracker.getDisplayName(uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTopLevelName() {
        if (displayName != null) return displayName;
        return name == null ? uuid.toString() : name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerProfile profile = (PlayerProfile) o;

        if (name != null ? !name.equals(profile.name) : profile.name != null) return false;
        if (!uuid.equals(profile.uuid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(PlayerProfile o) {
        if (this.name == null && o.name != null) {
            return -1;
        } else if (this.name != null && o.name == null) {
            return 1;
        }

        String name = ChatColor.stripColor(getTopLevelName());
        String oName = ChatColor.stripColor(o.getTopLevelName());

        return name.compareTo(oName);
    }

}