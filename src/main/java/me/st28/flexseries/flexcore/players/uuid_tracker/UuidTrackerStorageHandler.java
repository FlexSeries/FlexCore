package me.st28.flexseries.flexcore.players.uuid_tracker;

import java.util.Map;
import java.util.UUID;

abstract class UuidTrackerStorageHandler {

    PlayerUuidTracker uuidTracker;
    boolean isSync;

    UuidTrackerStorageHandler(PlayerUuidTracker uuidTracker, boolean isSync) {
        this.uuidTracker = uuidTracker;
        this.isSync = isSync;
    }

    /**
     * @return a map containing player UUIDs and names.
     */
    abstract Map<UUID, String> loadIndex();

    /**
     * Saves the player UUID and name map.
     */
    abstract void saveIndex(Map<UUID, String> index);

}