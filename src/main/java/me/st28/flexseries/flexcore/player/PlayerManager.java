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
import me.st28.flexseries.flexcore.player.loading.PlayerLoadCycle;
import me.st28.flexseries.flexcore.plugin.module.FlexModule;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.Map.Entry;

public final class PlayerManager extends FlexModule<FlexCore> implements Listener {

    private String unloadedMessage;

    private final List<String> loginMessageOrder = new ArrayList<>();

    /* Configuration Options */
    private long loadTimeout;
    private boolean enableJoinMessageChange;
    private boolean enableQuitMessageChange;

    final Set<UUID> handledPlayers = new HashSet<>();
    private final Map<UUID, PlayerLoadCycle> cachedCycles = new HashMap<>();

    public PlayerManager(FlexCore plugin) {
        super(plugin, "players", "Manages players", true);
    }

    @Override
    protected void handleReload() {
        FileConfiguration config = getConfig();

        loadTimeout = config.getLong("load timeout", 100L);
        enableJoinMessageChange = config.getBoolean("modify join message", true);
        enableQuitMessageChange = config.getBoolean("modify quit message", true);

        loginMessageOrder.clear();
        loginMessageOrder.addAll(config.getStringList("player join.message order"));

        unloadedMessage = StringEscapeUtils.unescapeJava(config.getString("player join.unloaded message", "Unable to load your data.\nPlease try again."));
    }

    private void handleOfficialLogin(final Player p, final PlayerLoadCycle cycle, final String joinMessage) {
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

        // Make sure the player's data was properly loaded.
        if (!handledPlayers.contains(uuid)) {
            p.kickPlayer(unloadedMessage);
            return;
        }

        if (enableJoinMessageChange) {
            e.setJoinMessage(null);
        }

        handleOfficialLogin(p, cachedCycles.remove(uuid), e.getJoinMessage());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuitHighest(PlayerQuitEvent e) {
        if (enableQuitMessageChange) {
            e.setQuitMessage(null);
        }

        handlePlayerLeave(e.getPlayer(), e.getQuitMessage());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent e) {
        handlePlayerLeave(e.getPlayer(), e.getLeaveMessage());
    }

    private void handlePlayerLeave(Player p, String message) {
        if (!handledPlayers.remove(p.getUniqueId())) {
            return;
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
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() != Result.ALLOWED) return;

        final UUID uuid = e.getUniqueId();
        if (uuid == null) return;
        handledPlayers.add(uuid);

        // Begin loading
        PlayerLoadCycle cycle = new PlayerLoadCycle(uuid, e.getName(), loadTimeout);

        final Object loadLock = new Object();

        cycle.startLoading(loadLock);

        synchronized (loadLock) {
            while (cycle.getLoadResult() == null) {
                try {
                    loadLock.wait();
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }

        if (!cycle.getLoadResult().isSuccess()) {
            e.disallow(Result.KICK_OTHER, cycle.getLoadResult().getFailMessage());
        } else {
            e.allow();

            cachedCycles.put(uuid, cycle);
        }
    }

}