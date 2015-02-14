package me.st28.flexseries.flexcore.players;

import java.util.UUID;

/**
 * The storage layer for the {@link me.st28.flexseries.flexcore.players.PlayerManager}.
 */
abstract class PlayerStorageHandler {

    PlayerManager playerManager;
    boolean isAsync;

    PlayerStorageHandler(PlayerManager playerManager, boolean isAsync) {
        this.playerManager = playerManager;
        this.isAsync = isAsync;
    }

    private PlayerData getPlayerData(UUID uuid) {
        return playerManager.loadedPlayers.get(uuid);
    }

    /**
     * Creates data for a player.
     *
     * @param uuid The UUID of the player to create data for.
     * @return The PlayerData instance for the player.<br />
     *         Should never be null.
     */
    abstract PlayerData createPlayerData(UUID uuid);

    /**
     * Loads a player's data.  If the player's data wasn't found, it should be created.
     *
     * @param uuid The UUID of the player to load.
     * @return The PlayerData instance for the player.<br />
     *         Null if the player's data hasn't been created.
     */
    abstract PlayerData loadPlayerData(UUID uuid);

    /**
     * Saves a player's data to the disk.
     *
     * @param data The loaded data instance.
     */
    abstract void savePlayerData(PlayerData data);

}