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
package me.st28.flexseries.flexcore.player;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.events.PlayerJoinLoadedEvent;
import me.st28.flexseries.flexcore.events.PlayerLeaveEvent;
import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.player.loading.LoaderOptions;
import me.st28.flexseries.flexcore.player.loading.PlayerLoadCycle;
import me.st28.flexseries.flexcore.player.loading.PlayerLoader;
import me.st28.flexseries.flexcore.plugin.module.FlexModule;
import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import me.st28.flexseries.flexcore.util.ArgumentCallback;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

//TODO: Freeze players on load
public final class PlayerManager extends FlexModule<FlexCore> implements Listener, PlayerLoader {

    private final List<String> loginMessageOrder = new ArrayList<>();

    /* Configuration Options */
    private long loadTimeout;
    private boolean enableJoinMessageChange;
    private boolean enableQuitMessageChange;

    private int autoUnloadIntervalTaskId = -1;

    private File playerDir;
    private final Map<UUID, PlayerData> playerData = new HashMap<>();

    public PlayerManager(FlexCore plugin) {
        super(plugin, "players", "Manages players", true, new LoaderOptions().setRequired(true));
    }

    @Override
    protected void handleLoad() {
        playerDir = new File(getDataFolder() + File.separator + "userdata");
        playerDir.mkdirs();
    }

    @Override
    protected void handleReload() {
        playerDir.mkdirs();

        FileConfiguration config = getConfig();

        loadTimeout = config.getLong("load timeout", 100L);
        enableJoinMessageChange = config.getBoolean("modify join message", true);
        enableQuitMessageChange = config.getBoolean("modify quit message", true);

        int autoUnloadInterval = config.getInt("auto unload interval", 5);
        if (autoUnloadIntervalTaskId != -1) {
            Bukkit.getScheduler().cancelTask(autoUnloadIntervalTaskId);
        }

        if (autoUnloadInterval > 0) {
            autoUnloadIntervalTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                Iterator<Entry<UUID, PlayerData>> iterator = playerData.entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry<UUID, PlayerData> next = iterator.next();

                    if (Bukkit.getPlayer(next.getKey()) == null) {
                        savePlayer(next.getKey());
                        iterator.remove();
                    }
                }
            }, autoUnloadInterval * 1200L, autoUnloadInterval * 1200L);
        }

        loginMessageOrder.clear();
        loginMessageOrder.addAll(config.getStringList("player join.message order"));
    }

    @Override
    protected void handleSave(boolean async) {
        for (UUID uuid : playerData.keySet()) {
            savePlayer(uuid);
        }
    }

    private void savePlayer(UUID uuid) {
        PlayerData data = getPlayerData(uuid);
        if (data == null) return;

        YamlFileManager file = new YamlFileManager(playerDir + File.separator + uuid.toString() + ".yml");

        data.save(file.getConfig());

        file.save();
    }

    @Override
    public void loadPlayer(UUID uuid, String name, PlayerLoadCycle cycle) {
        getPlayerData(uuid);

        PlayerLoadCycle.setLoaderSuccess(cycle, this);
    }

    /**
     * @return a {@link PlayerData} instance for a specified player.
     */
    public PlayerData getPlayerData(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null.");
        if (!playerData.containsKey(uuid)) {
            PlayerData data = new PlayerData(uuid, new YamlFileManager(playerDir + File.separator + uuid.toString() + ".yml").getConfig());
            playerData.put(uuid, data);
        }
        return playerData.get(uuid);
    }

    private void handleOfficialLogin(final Player p, final PlayerLoadCycle cycle, final String joinMessage) {
        PlayerData playerData = getPlayerData(p.getUniqueId());
        if (playerData.firstJoin == null) {
            playerData.firstJoin = System.currentTimeMillis();
        }
        playerData.lastLogin = System.currentTimeMillis();

        String curIp = p.getAddress().toString();
        playerData.lastIp = curIp;
        if (!playerData.ips.contains(curIp)) {
            playerData.ips.add(curIp);
        }

        String curName = p.getName();
        playerData.lastName = curName;
        if (!playerData.names.contains(curName)) {
            playerData.names.add(curName);
        }

        PlayerJoinLoadedEvent newJoinEvent = new PlayerJoinLoadedEvent(p, cycle.getCustomData());
        if (enableJoinMessageChange) {
            newJoinEvent.setJoinMessage(MessageReference.createPlain(joinMessage));
        }

        Bukkit.getPluginManager().callEvent(newJoinEvent);

        if (enableJoinMessageChange) {
            for (Player op : Bukkit.getOnlinePlayers()) {
                MessageReference message = newJoinEvent.getJoinMessage(op.getUniqueId());

                if (message != null) {
                    message.sendTo(op);
                }
            }
        }

        Map<String, MessageReference> loginMessages = newJoinEvent.getLoginMessages();
        List<String> sent = new ArrayList<>();

        for (String identifier : loginMessageOrder) {
            MessageReference message = loginMessages.get(identifier);
            if (message != null) {
                message.sendTo(p);
                sent.add(identifier);
            }
        }

        if (sent.size() != loginMessages.size()) {
            for (Entry<String, MessageReference> entry : loginMessages.entrySet()) {
                if (!sent.contains(entry.getKey())) {
                    entry.getValue().sendTo(p);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinHighest(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        String joinMessage = e.getJoinMessage();

        ArgumentCallback<PlayerLoadCycle> callback = new ArgumentCallback<PlayerLoadCycle>() {
            @Override
            public boolean isSynchronous() {
                return true;
            }

            @Override
            public void callback(PlayerLoadCycle argument) {
                Player player = Bukkit.getPlayer(argument.getPlayerUuid());
                if (player != null) {
                    handleOfficialLogin(player, argument, joinMessage);
                }
            }
        };

        PlayerLoadCycle cycle = new PlayerLoadCycle(uuid, p.getName(), loadTimeout);
        cycle.startLoading(callback);

        if (enableJoinMessageChange) {
            e.setJoinMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuitHighest(PlayerQuitEvent e) {
        handlePlayerLeave(e.getPlayer(), e.getQuitMessage());

        if (enableQuitMessageChange) {
            e.setQuitMessage(null);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent e) {
        handlePlayerLeave(e.getPlayer(), e.getLeaveMessage());
    }

    private void handlePlayerLeave(Player p, String message) {
        boolean isPlayerLoaded = playerData.containsKey(p.getUniqueId());

        if (isPlayerLoaded) {
            getPlayerData(p.getUniqueId()).lastLogout = System.currentTimeMillis();
        }

        PlayerLeaveEvent newLeaveEvent = new PlayerLeaveEvent(p);
        if (enableQuitMessageChange) {
            newLeaveEvent.setLeaveMessage(MessageReference.createPlain(message));
        }

        Bukkit.getPluginManager().callEvent(newLeaveEvent);

        if (enableQuitMessageChange) {
            for (Player op : Bukkit.getOnlinePlayers()) {
                MessageReference plMessage = newLeaveEvent.getLeaveMessage(op.getUniqueId());

                if (plMessage != null) {
                    plMessage.sendTo(op);
                }
            }
        }

        if (isPlayerLoaded) {
            savePlayer(p.getUniqueId());
        }
    }

}