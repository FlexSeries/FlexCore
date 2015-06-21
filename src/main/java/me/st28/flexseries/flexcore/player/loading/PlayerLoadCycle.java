/**
 * FlexCore - Licensed under the MIT License (MIT)
 *
 * Copyright (c) Stealth2800 <http://stealthyone.com/>
 * Copyright (c) contributors <https://github.com/FlexSeries>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.st28.flexseries.flexcore.player.loading;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.events.PlayerJoinLoadedEvent;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.util.ArgumentCallback;
import me.st28.flexseries.flexcore.util.TaskChain;
import me.st28.flexseries.flexcore.util.TaskChain.GenericTask;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the loading process for a player that connects to the server.
 */
public final class PlayerLoadCycle {

    private static FlexCore getPluginInstance() {
        return FlexCore.getInstance();
    }

    private static Map<PlayerLoader, LoaderOptions> registeredLoaders = new HashMap<>();

    private static LoaderOptions getLoaderOptions(PlayerLoader loader) {
        return registeredLoaders.get(loader);
    }

    /**
     * Registers a {@link PlayerLoader}.
     *
     * @param playerLoader PlayerLoader to register
     * @return True if registered.<br />
     *         False if already registered.
     */
    public static boolean registerLoader(PlayerLoader playerLoader, LoaderOptions options) {
        Validate.notNull(playerLoader, "Player loader cannot be null.");
        if (options == null) options = new LoaderOptions();

        if (registeredLoaders.containsKey(playerLoader)) {
            return false;
        }

        LogHelper.debug(FlexCore.class, "Registered PlayerLoader: '" + playerLoader.getClass().getCanonicalName() + "' (required: " + options.isRequired() + ")");
        registeredLoaders.put(playerLoader, options);
        return true;
    }

    /**
     * Utility method to indicate that a {@link PlayerLoader} successfully loaded a player's data.
     */
    public static void setLoaderSuccess(PlayerLoadCycle cycle, PlayerLoader playerLoader) {
        Validate.notNull(cycle, "Cycle cannot be null.");
        Validate.notNull(playerLoader, "Player loader cannot be null.");

        cycle.loaderStatuses.put(playerLoader.getClass().getCanonicalName(), PlayerLoaderStatus.SUCCEEDED);
        LogHelper.debug(FlexCore.class, "Player loader '" + playerLoader.getClass().getCanonicalName() + "' successfully loaded player data.");

        cycle.isFullyLoaded();
    }

    /**
     * Utility method to indicate that a {@link PlayerLoader} failed to load a player's data.
     */
    public static void setLoaderFailure(PlayerLoadCycle cycle, PlayerLoader playerLoader) {
        Validate.notNull(cycle, "Cycle cannot be null.");
        Validate.notNull(playerLoader, "Player loader cannot be null.");

        cycle.loaderStatuses.put(playerLoader.getClass().getCanonicalName(), PlayerLoaderStatus.FAILED);
        LogHelper.debug(FlexCore.class, "Player loader '" + playerLoader.getClass().getCanonicalName() + "' failed to load player data.");

        if (registeredLoaders.get(playerLoader).isRequired()) {
            LogHelper.debug(FlexCore.class, "Kicking player because player loader '" + playerLoader.getClass().getCanonicalName() + "' is required.");
            cycle.kickPlayer();
            return;
        }
    }

    private final UUID playerUuid;
    private final String playerName;
    private final Map<String, Object> customData = new ConcurrentHashMap<>();

    private int timeoutTaskId = -1;
    private int taskId = -1;

    private final Map<String, PlayerLoaderStatus> loaderStatuses = new ConcurrentHashMap<>();

    private ArgumentCallback<PlayerLoadCycle> callback;

    /**
     * Creates a new player loading instance.
     *
     * @param playerUuid The UUID of the player being loaded.
     * @param playerName The name of the player being loaded.
     * @param timeout The maximum time (in ticks) that this loader can be running.
     */
    public PlayerLoadCycle(UUID playerUuid, String playerName, long timeout) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;

        for (PlayerLoader loader : registeredLoaders.keySet()) {
            loaderStatuses.put(loader.getClass().getCanonicalName(), PlayerLoaderStatus.NOT_STARTED);
        }

        timeoutTaskId = Bukkit.getScheduler().runTaskLaterAsynchronously(getPluginInstance(), () -> {
            //timed out
            LogHelper.debug(FlexCore.class, "Player load cycle for '" + playerUuid + "' timed out.");

            for (Entry<PlayerLoader, LoaderOptions> entry : registeredLoaders.entrySet()) {
                if (loaderStatuses.get(entry.getKey().getClass().getCanonicalName()) != PlayerLoaderStatus.SUCCEEDED && entry.getValue().isRequired()) {
                    kickPlayer();
                    return;
                }
            }

            cancelTasks(true);
            callback.run(PlayerLoadCycle.this);
        }, timeout).getTaskId();
    }

    /**
     * @return the UUID of the player the cycle is loading.
     */
    public final UUID getPlayerUuid() {
        return playerUuid;
    }

    /**
     * @return the name of the player the cycle is loading.
     */
    public final String getPlayerName() {
        return playerName;
    }

    /**
     * @return custom data to include in the {@link PlayerJoinLoadedEvent}.
     */
    public final Map<String, Object> getCustomData() {
        return customData;
    }

    /**
     * Begins the load cycle.
     */
    public void startLoading(ArgumentCallback<PlayerLoadCycle> callback) {
        Validate.notNull(callback, "Callback cannot be null.");
        this.callback = callback;

        for (PlayerLoader playerLoader : registeredLoaders.keySet()) {
            startLoader(playerLoader);
            LogHelper.debug(FlexCore.class, "Starting player loader: '" + playerLoader.getClass().getCanonicalName() + "'");
        }

        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(getPluginInstance(), this::checkStartStatuses, 2L, 2L).getTaskId();
    }

    /**
     * @return True if all of the {@link PlayerLoader}s have completed loading for a given player.<br />
     *         Will also end the loader and officially make the player join the server.
     */
    public boolean isFullyLoaded() {
        for (PlayerLoaderStatus status : loaderStatuses.values()) {
            if (status != PlayerLoaderStatus.SUCCEEDED && status != PlayerLoaderStatus.FAILED) {
                return false;
            }
        }

        cancelTasks(true);

        callback.run(PlayerLoadCycle.this);
        return true;
    }

    /**
     * Attempts to start a specified {@link PlayerLoader}.
     *
     * @param playerLoader The loader to start.
     */
    private void startLoader(PlayerLoader playerLoader) {
        try {
            List<String> dependencies = getLoaderOptions(playerLoader).getDependencies();
            if (dependencies != null && !dependencies.isEmpty()) {
                for (String dependency : dependencies) {
                    PlayerLoaderStatus depStatus = loaderStatuses.get(dependency);
                    if (depStatus == null) {
                        LogHelper.debug(FlexCore.class, "Loader dependency '" + dependency + "' for loader '" + playerLoader.getClass().getCanonicalName() + "'not found.");

                        if (getLoaderOptions(playerLoader).isRequired()) {
                            kickPlayer();
                            return;
                        }
                        continue;
                    }

                    switch (depStatus) {
                        case NOT_STARTED:
                        case STARTED:
                            LogHelper.debug(FlexCore.class, "Unable to start loader '" + playerLoader.getClass().getCanonicalName() + "': dependency '" + dependency + "' not loaded yet.");
                            return;

                        case FAILED:
                            if (getLoaderOptions(playerLoader).isRequired()) {
                                loaderStatuses.put(playerLoader.getClass().getCanonicalName(), PlayerLoaderStatus.FAILED);
                                kickPlayer();
                                return;
                            }

                            break;

                        case SUCCEEDED:
                            break;

                        default:
                    }
                }
            }

            LogHelper.debug(FlexCore.class, "Starting player loader '" + playerLoader.getClass().getCanonicalName() + "'");
            if (loaderStatuses.get(playerLoader.getClass().getCanonicalName()) == PlayerLoaderStatus.NOT_STARTED) {
                loaderStatuses.put(playerLoader.getClass().getCanonicalName(), PlayerLoaderStatus.STARTED);
            }
            playerLoader.loadPlayer(playerUuid, playerName, this);
        } catch (Exception ex) {
            LogHelper.severe(FlexCore.class, "An exception occurred while loading player '" + playerName + "' (" + playerUuid.toString() + ")");
            PlayerLoadCycle.setLoaderFailure(this, playerLoader);
            ex.printStackTrace();
        }
    }

    /**
     * Checks to see if all of the player loaders have been started.
     */
    private void checkStartStatuses() {
        LogHelper.debug(FlexCore.class, "Checking player loader statuses");

        boolean allStarted = true;
        for (PlayerLoader playerLoader : registeredLoaders.keySet()) {
            LogHelper.debug(FlexCore.class, "Checking player loader status: '" + playerLoader.getClass().getCanonicalName() + "'");

            if (loaderStatuses.get(playerLoader.getClass().getCanonicalName()) == PlayerLoaderStatus.NOT_STARTED) {
                LogHelper.debug(FlexCore.class, "Player loader '" + playerLoader.getClass().getCanonicalName() + "' hasn't started yet.");

                if (!getLoaderOptions(playerLoader).isAsynchronous()) {
                    new TaskChain().add(new GenericTask() {
                        @Override
                        protected void run() {
                            startLoader(playerLoader);
                        }
                    }).execute();
                } else {
                    startLoader(playerLoader);
                }

                allStarted = false;
            }
        }

        if (allStarted) {
            cancelTasks(false);
        }
    }

    /**
     * Cancels the runnable that attempts to start loaders that haven't begun loading yet.
     */
    private void cancelTasks(boolean cancelTimeout) {
        if (cancelTimeout && timeoutTaskId != -1) {
            Bukkit.getScheduler().cancelTask(timeoutTaskId);
            timeoutTaskId = -1;
        }

        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    /**
     * Kicks a player due to a failed load.
     */
    private void kickPlayer() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPluginInstance(), () -> {
            Player p = Bukkit.getPlayer(playerUuid);
            if (p == null) {
                return;
            }

            p.kickPlayer("An error occurred while trying to load your data.\nPlease contact an administrator and notify them.");
        });
        cancelTasks(true);
    }

}