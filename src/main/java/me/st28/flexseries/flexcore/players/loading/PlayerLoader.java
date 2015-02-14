package me.st28.flexseries.flexcore.players.loading;

import java.util.UUID;

/**
 * Represents a class that loads player data on join.<br />
 * Must be registered using {@link me.st28.flexseries.flexcore.players.loading.PlayerLoadCycle#registerLoader(PlayerLoader)}
 */
public interface PlayerLoader {

    /**
     * @return true if the loader should be called synchronously.
     */
    boolean isPlayerLoadSync();

    /**
     * Loads the player data for the manager.
     *
     * @param uuid Player UUID we're loading.
     * @param name Player name we're loading.
     * @param cycle The load cycle that this player loader is loading for.
     * @return True if the loading began successfully.<br />
     *         False if the loading is unable to start yet.
     */
    boolean loadPlayer(UUID uuid, String name, PlayerLoadCycle cycle);

}