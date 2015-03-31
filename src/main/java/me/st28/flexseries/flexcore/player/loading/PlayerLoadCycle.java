package me.st28.flexseries.flexcore.player.loading;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.logging.LogHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Handles the loading process for a player that connects to the server.
 */
public final class PlayerLoadCycle {

    private static FlexCore getPluginInstance() {
        return FlexCore.getInstance();
    }

    /**
     * Utility method for ending a cycle due to a {@link PlayerLoader} failing.
     *
     * @param cycle The load cycle instance.
     * @param playerLoader The loader that failed to load the player's data.
     */
    public static void failedCycle(PlayerLoadCycle cycle, PlayerLoader playerLoader) {
        LogHelper.debug(FlexCore.class, playerLoader.getClass().getCanonicalName() + " failed to load player data.");
        cycle.handleFailedLoad();
        cycle.setLoadResult(new LoadResult(false, "Failed to load data"));
    }

    /**
     * Utility method for marking a {@link PlayerLoader} as complete in a given cycle.
     *
     * @param cycle The load cycle instance.
     * @param playerLoader The loader that just completed loading the player.
     */
    public static void completedCycle(PlayerLoadCycle cycle, PlayerLoader playerLoader) {
        LogHelper.debug(FlexCore.class, playerLoader.getClass().getCanonicalName() + " COMPLETED cycle.");
        cycle.completedLoaders.add(playerLoader.getClass().getCanonicalName());
        cycle.isFullyLoaded();
    }

    private static Set<PlayerLoader> registeredLoaders = new HashSet<>();

    /**
     * Registers a {@link PlayerLoader}.
     *
     * @param playerLoader PlayerLoader to register
     * @return True if registered.<br />
     *         False if already registered.
     */
    public static boolean registerLoader(PlayerLoader playerLoader) {
        boolean result = registeredLoaders.add(playerLoader);
        if (result) {
            LogHelper.debug(FlexCore.class, "Registered PlayerLoader: " + playerLoader.getClass().getCanonicalName());
        }
        return result;
    }

    private UUID playerUuid;
    private String playerName;
    private int taskId = -1;
    private Set<String> startedLoaders = new HashSet<>();
    Set<String> completedLoaders = new HashSet<>();
    private Map<String, Object> customData = new HashMap<>();

    LoadResult loadResult;

    Object loadLock;

    public PlayerLoadCycle(UUID playerUuid, String playerName, long timeout) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;

        Bukkit.getScheduler().runTaskLaterAsynchronously(getPluginInstance(), new Runnable() {
            @Override
            public void run() {
                if (loadResult == null) {
                    setLoadResult(new LoadResult(false, "Load timed out"));
                }
            }
        }, timeout);
    }

    private void setLoadResult(LoadResult loadResult) {
        this.loadResult = loadResult;
        synchronized (loadLock) {
            loadLock.notify();
        }
    }

    /**
     * @return the UUID of the player the cycle is loading.
     */
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    /**
     * @return the name of the player the cycle is loading.
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * @return custom data to include in the PlayerJoinLoadedEvent.
     */
    public Map<String, Object> getCustomData() {
        return customData;
    }

    public final LoadResult getLoadResult() {
        return loadResult;
    }

    /**
     * @return True if all of the {@link PlayerLoader}s have completed loading for a given player.<br />
     *         Will also end the loader and officially make the player join the server.
     */
    public boolean isFullyLoaded() {
        if (completedLoaders.size() != registeredLoaders.size()) {
            return false;
        }

        cancelTask();
        setLoadResult(new LoadResult(true, null));
        return true;
    }

    /**
     * @return True if the specified loader has completed loading.
     */
    public boolean isLoaderComplete(Class<? extends PlayerLoader> clazz) {
        return completedLoaders.contains(clazz.getCanonicalName());
    }

    /**
     * Begins the load cycle.
     */
    public void startLoading(Object loadLock) {
        this.loadLock = loadLock;
        for (PlayerLoader playerLoader : registeredLoaders) {
            startLoader(playerLoader);
            LogHelper.debug(FlexCore.class, "Starting loader: " + playerLoader.getClass().getCanonicalName());
        }

        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(getPluginInstance(), new Runnable() {
            @Override
            public void run() {
                checkStartStates();
            }
        }, 2L, 2L).getTaskId();
    }

    /**
     * Attempts to start a specified {@link PlayerLoader}.
     *
     * @param playerLoader The loader to start.
     */
    private void startLoader(PlayerLoader playerLoader) {
        try {
            if (playerLoader.loadPlayer(playerUuid, playerName, this)) {
                startedLoaders.add(playerLoader.getClass().getCanonicalName());
                LogHelper.debug(FlexCore.class, playerLoader.getClass().getCanonicalName() + " BEGAN cycle.");
            }
        } catch (Exception ex) {
            PlayerLoadCycle.failedCycle(this, playerLoader);
            LogHelper.severe(FlexCore.class, "An exception occurred while loading player '" + playerName + "' (" + playerUuid.toString() + ")");
            ex.printStackTrace();
        }
    }

    /**
     * Checks to see if all of the player loaders have been started.
     */
    private void checkStartStates() {
        boolean allStarted = true;
        LogHelper.debug(FlexCore.class, "Checking load states");
        for (PlayerLoader playerLoader : registeredLoaders) {
            LogHelper.debug(FlexCore.class, "Checking load state for " + playerLoader.getClass().getCanonicalName());
            if (!startedLoaders.contains(playerLoader.getClass().getCanonicalName())) {
                LogHelper.debug(FlexCore.class, "Loader " + playerLoader.getClass().getCanonicalName() + " hasn't started yet.");
                startLoader(playerLoader);
                allStarted = false;
            }
        }

        if (allStarted) {
            cancelTask();
        }
    }

    /**
     * Cancels the runnable that attempts to start loaders that haven't begun loading yet.
     */
    private void cancelTask() {
        Bukkit.getScheduler().cancelTask(taskId);
    }

    /**
     * Handles a loader failing to load a player.
     */
    private void handleFailedLoad() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPluginInstance(), new Runnable() {
            @Override
            public void run() {
                Player p = Bukkit.getPlayer(playerUuid);
                if (p == null) {
                    return;
                }

                setLoadResult(new LoadResult(false, "An error occurred while trying to load your data.\nPlease contact an administrator and notify them."));
            }
        });
        cancelTask();
    }

}