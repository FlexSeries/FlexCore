package me.st28.flexseries.flexcore.players;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.events.NewPlayerJoinEvent;
import me.st28.flexseries.flexcore.events.PlayerJoinLoadedEvent;
import me.st28.flexseries.flexcore.events.PlayerLeaveEvent;
import me.st28.flexseries.flexcore.hooks.HookManager;
import me.st28.flexseries.flexcore.hooks.VanishNoPacketHook;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.messages.ReplacementMap;
import me.st28.flexseries.flexcore.players.loading.PlayerLoadCycle;
import me.st28.flexseries.flexcore.players.loading.PlayerLoader;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.storage.StorageType;
import me.st28.flexseries.flexcore.utils.ArgumentCallback;
import me.st28.flexseries.flexcore.utils.ScreenTitle;
import me.st28.flexseries.flexcore.utils.TaskChain;
import me.st28.flexseries.flexcore.utils.TaskChain.AsyncGenericTask;
import me.st28.flexseries.flexcore.utils.TaskChain.VariableGenericTask;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import java.io.File;
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;

public final class PlayerManager extends FlexModule<FlexCore> implements Listener, PlayerLoader {

    private String unloadedMessage;
    private String titleFirstLine;
    private String titleSecondLineFirstJoin;
    private String titleSecondLineJoin;

    private final List<String> loginMessageOrder = new ArrayList<>();

    /* Configuration Options */
    private long loadTimeout;
    private boolean enableLoginTitle;
    private boolean enableJoinMessageChange;
    private boolean enableJoinMessageCompatibility;
    private boolean enableQuitMessageChange;
    private boolean enableQuitMessageCompatibility;

    private PlayerStorageHandler storageHandler;

    private File playerDir;
    final Set<UUID> handledPlayers = new HashSet<>();
    final Map<UUID, PlayerData> loadedPlayers = new HashMap<>();
    private final Map<UUID, PlayerLoadCycle> cachedCycles = new HashMap<>();

    public PlayerManager(FlexCore plugin) {
        super(plugin, "players", "Manages players", true);
    }

    @Override
    protected void handleLoad() throws Exception {
        playerDir = new File(getDataFolder() + File.separator + "data");
    }

    @Override
    protected void handleReload() {
        playerDir.mkdirs();

        FileConfiguration config = getConfig();

        StorageType storageType = StorageType.valueOf(config.getString("storage", "").toUpperCase());

        switch (storageType) {
            /*case MYSQL:*/

            case YAML:
                storageHandler = new PlayerStorageHandlerYaml(this);
                break;

            default:
                throw new IllegalArgumentException("Unsupported storage type: '" + storageType + "'");
        }

        loadTimeout = config.getLong("load timeout", 100L);

        enableLoginTitle = config.getBoolean("login title", true);
        enableJoinMessageChange = config.getBoolean("join message.enabled", true);
        enableJoinMessageCompatibility = config.getBoolean("join message.compatibility", true);
        enableQuitMessageChange = config.getBoolean("quit message.enabled", true);
        enableQuitMessageCompatibility = config.getBoolean("quit message.compatibility", true);

        loginMessageOrder.clear();
        loginMessageOrder.addAll(config.getStringList("player join.message order"));

        unloadedMessage = StringEscapeUtils.unescapeJava(config.getString("player join.unloaded message", "The server is not loaded yet.\nPlease try again."));

        titleFirstLine = config.getString("login title.message.first line", "");
        titleSecondLineFirstJoin = config.getString("login title.message.second line.first join", "");
        titleSecondLineJoin = config.getString("login title.message.second line.join", "");
    }

    @Override
    protected void handleSave(boolean async) {
        savePlayers(loadedPlayers.keySet(), async);
    }

    private void savePlayer(UUID uuid, boolean async) {
        savePlayers(Arrays.asList(uuid), async);
    }

    private void savePlayers(final Collection<UUID> uuids, boolean async) {
        new TaskChain().add(new VariableGenericTask(async) {
            @Override
            protected void run() {
                for (UUID uuid : uuids) {
                    PlayerData data = loadedPlayers.get(uuid);

                    if (data != null) {
                        storageHandler.savePlayerData(data);
                    }
                }
            }
        }).execute();
    }

    public final File getPlayerDir() {
        return playerDir;
    }

    /**
     * @return a player's data.<br />
     *         If not loaded, will load (POTENTIALLY NOT THREAD-SAFE).<br />
     *         Null if the player's data doesn't exist.
     */
    public PlayerData getPlayerData(UUID uuid) {
        if (!loadedPlayers.containsKey(uuid)) {
            loadedPlayers.put(uuid, storageHandler.loadPlayerData(uuid));
        }

        return loadedPlayers.get(uuid);
    }

    /**
     * Retrieves a player's data asynchronously.
     *
     * @see #getPlayerData(java.util.UUID)
     */
    public void getPlayerDataAsync(final UUID uuid, final ArgumentCallback<PlayerData> callback) {
        if (loadedPlayers.containsKey(uuid)) {
            ArgumentCallback.callCallback(callback, loadedPlayers.get(uuid));
        } else {
            new TaskChain().add(new AsyncGenericTask() {
                @Override
                protected void run() {
                    ArgumentCallback.callCallback(callback, getPlayerData(uuid));
                }
            }).execute();
        }
    }

    private void handleOfficialLogin(final Player p, final PlayerLoadCycle cycle, final PlayerData data, final String defaultJoinMessage) {
        final long curTime = System.currentTimeMillis();
        data.lastLogin = new Timestamp(curTime);

        boolean isVanished = p.hasMetadata("vanished") && p.getMetadata("vanished").get(0).asBoolean();
        boolean firstJoin = (boolean) cycle.getCustomData().get("firstJoin");

        PlayerJoinLoadedEvent newJoinEvent = new PlayerJoinLoadedEvent(p, cycle.getCustomData());

        if (defaultJoinMessage != null) {
            newJoinEvent.setJoinMessage(MessageReference.createPlain(defaultJoinMessage));
        }

        String secondLine;
        if (firstJoin) {
            data.firstJoin = new Timestamp(curTime);

            NewPlayerJoinEvent newPlayerJoinEvent = new NewPlayerJoinEvent(p);
            Bukkit.getPluginManager().callEvent(newPlayerJoinEvent);

            secondLine = ChatColor.translateAlternateColorCodes('&', titleSecondLineFirstJoin.replace("{DISPNAME}", p.getDisplayName()));

            if (defaultJoinMessage == null) {
                newJoinEvent.setJoinMessage(MessageReference.create(FlexCore.class, "players.notices.first_join", new ReplacementMap("{DISPNAME}", p.getDisplayName()).getMap()));
            }
        } else {
            secondLine = ChatColor.translateAlternateColorCodes('&', titleSecondLineJoin.replace("{DISPNAME}", p.getDisplayName()));

            if (defaultJoinMessage == null) {
                newJoinEvent.setJoinMessage(MessageReference.create(FlexCore.class, "players.notices.join", new ReplacementMap("{DISPNAME}", p.getDisplayName()).getMap()));
            }
        }

        if (enableLoginTitle) {
            ScreenTitle title = new ScreenTitle(ChatColor.translateAlternateColorCodes('&', titleFirstLine.replace("{SERVER}", plugin.getServerName())), secondLine);
            title.setFadeInTime(1);
            title.setStayTime(3);
            title.setFadeOutTime(1);
            title.send(p);
        }

        if (isVanished) {
            newJoinEvent.setJoinMessage(null);
        }

        Bukkit.getPluginManager().callEvent(newJoinEvent);

        for (Player op : Bukkit.getOnlinePlayers()) {
            MessageReference message = newJoinEvent.getJoinMessage(op.getUniqueId());

            if (message != null) {
                message.sendTo(op);
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinLowest(PlayerJoinEvent e) {
        if (enableJoinMessageChange) {
            e.setJoinMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinHighest(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();

        if (!handledPlayers.contains(uuid)) {
            p.kickPlayer(unloadedMessage);
            return;
        }

        if (enableJoinMessageChange && !enableJoinMessageCompatibility) {
            e.setJoinMessage(null);
        }

        handleOfficialLogin(p, cachedCycles.remove(uuid), getPlayerData(uuid), e.getJoinMessage());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuitLowest(PlayerQuitEvent e) {
        if (enableQuitMessageChange) {
            e.setQuitMessage(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuitHighest(PlayerQuitEvent e) {
        if (enableQuitMessageChange && !enableQuitMessageCompatibility) {
            e.setQuitMessage(null);
        }

        handlePlayerLeave(e.getPlayer(), e.getQuitMessage());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerKick(PlayerKickEvent e) {
        handlePlayerLeave(e.getPlayer(), e.getLeaveMessage());
    }

    private void handlePlayerLeave(Player p, String message) {
        if (!handledPlayers.remove(p.getUniqueId())) {
            return;
        }

        boolean isVanished;
        try {
            isVanished = FlexPlugin.getRegisteredModule(HookManager.class).getHook(VanishNoPacketHook.class).isPlayerVanished(p);
        } catch (Exception ex) {
            isVanished = false;
        }

        PlayerLeaveEvent newLeaveEvent = new PlayerLeaveEvent(p);

        if (message == null) {
            if (!isVanished) {
                newLeaveEvent.setLeaveMessage(MessageReference.create(FlexCore.class, "players.notices.quit", new ReplacementMap("{DISPNAME}", p.getDisplayName()).getMap()));
            }
        } else {
            newLeaveEvent.setLeaveMessage(MessageReference.createPlain(message));
        }

        Bukkit.getPluginManager().callEvent(newLeaveEvent);

        for (Player op : Bukkit.getOnlinePlayers()) {
            MessageReference plMessage = newLeaveEvent.getLeaveMessage(op.getUniqueId());

            if (plMessage != null) {
                plMessage.sendTo(op);
            }
        }

        PlayerData data = getPlayerData(p.getUniqueId());
        if (data != null) {
            storageHandler.savePlayerData(data);
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

    @Override
    public boolean isPlayerLoadSync() {
        return !storageHandler.isAsync;
    }

    @Override
    public boolean loadPlayer(final UUID uuid, String name, final PlayerLoadCycle cycle) {
        if (loadedPlayers.containsKey(uuid)) {
            PlayerLoadCycle.completedCycle(cycle, this);
            cycle.getCustomData().put("firstJoin", false);
            return true;
        }

        new TaskChain().add(new VariableGenericTask(storageHandler.isAsync) {
            @Override
            protected void run() {
                loadPlayer(uuid, cycle);
            }
        }).execute();

        return true;
    }

    private void loadPlayer(UUID uuid, PlayerLoadCycle cycle) {
        if (getPlayerData(uuid) != null) {
            cycle.getCustomData().put("firstJoin", false);

            PlayerLoadCycle.completedCycle(cycle, PlayerManager.this);
        } else {
            cycle.getCustomData().put("firstJoin", true);

            PlayerData data = storageHandler.createPlayerData(uuid);
            if (data == null) {
                PlayerLoadCycle.failedCycle(cycle, PlayerManager.this);
            } else {
                loadedPlayers.put(uuid, data);
                PlayerLoadCycle.completedCycle(cycle, PlayerManager.this);
            }
        }
    }

}