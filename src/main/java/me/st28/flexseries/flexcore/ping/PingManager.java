package me.st28.flexseries.flexcore.ping;

import com.comphenix.packetwrapper.WrapperStatusServerOutServerInfo;
import com.comphenix.protocol.PacketType.Status.Server;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.google.gson.JsonParser;
import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.hooks.HookManager;
import me.st28.flexseries.flexcore.hooks.ProtocolLibHook;
import me.st28.flexseries.flexcore.hooks.exceptions.HookDisabledException;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.plugins.exceptions.ModuleDisabledException;
import me.st28.flexseries.flexcore.utils.InternalUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PingManager extends FlexModule<FlexCore> implements Listener {

    private Object objectMinecraftServer;
    private Field fieldMotd;

    private String currentMessage = null;
    private String noMessage;
    private Map<String, String[]> messages = new LinkedHashMap<>();

    public PingManager(FlexCore plugin) {
        super(plugin, "ping", "Dynamic server list message", HookManager.class);
    }

    @Override
    protected void handleLoad() throws Exception {
        // Vanilla modification version
        try {
            org.bukkit.Server bukkit = Bukkit.getServer();

            Class classCraftServer = InternalUtils.getCBClass("CraftServer");
            Field fieldConsole = classCraftServer.getDeclaredField("console");
            fieldConsole.setAccessible(true);

            objectMinecraftServer = fieldConsole.get(bukkit);

            Class classMinecraftServer = InternalUtils.getNMSClass("MinecraftServer");
            fieldMotd = classMinecraftServer.getDeclaredField("motd");
            fieldMotd.setAccessible(true);
        } catch (Exception ex) {
            LogHelper.warning(this, "Unable to enable standard server list modifier (" + ex.getMessage() + ") - defaulting to event-based modifier.");
            objectMinecraftServer = null;
            fieldMotd = null;
        }

        // ProtocolLib character fix
        try {
            FlexPlugin.getRegisteredModule(HookManager.class).getHook(ProtocolLibHook.class).getProtocolManager().addPacketListener(new PacketAdapter(plugin, Server.OUT_SERVER_INFO) {
                @Override
                public void onPacketSending(PacketEvent event) {
                    WrapperStatusServerOutServerInfo wrapper = new WrapperStatusServerOutServerInfo(event.getPacket());

                    wrapper.setServerPing(WrappedServerPing.fromJson(new JsonParser().parse(wrapper.getServerPing().toJson()).getAsJsonObject().toString().replace("\\\\", "\\")));
                }
            });
        } catch (ModuleDisabledException ex) {
            LogHelper.warning(this, "Unable to enable server list character fix: " + ex.getMessage());
        } catch (HookDisabledException ex) {
            LogHelper.warning(this, "Unable to enable server list character fix because ProtocolLib is not installed on the server.");
        }
    }

    @Override
    protected void handleReload() {
        FileConfiguration config = getConfig();

        noMessage = config.getString("none set", "&c{SERVER} &8- &4&oNo message set.");

        ConfigurationSection msgSec = config.getConfigurationSection("Messages");
        messages.clear();
        if (msgSec != null) {
            for (String key : msgSec.getKeys(false)) {
                messages.put(key.toLowerCase(), msgSec.getStringList(key).toArray(new String[2]));
            }
        }

        currentMessage = null;
        String curMessageName = config.getString("current", "default").toLowerCase();
        setCurrentMessage(curMessageName);
        currentMessage = curMessageName;
    }

    @Override
    protected void handleSave(boolean async) {
        getConfig().set("current", currentMessage);
    }

    public String getCurrentMessageName() {
        return currentMessage;
    }

    public String getMessage(String name) {
        Validate.notNull(name, "Name cannot be null.");
        String returnMessage;

        String[] cur = messages.get(name.toLowerCase());
        if (cur == null) {
            returnMessage = noMessage;
        } else if (cur[1] == null) {
            returnMessage = cur[0];
        } else {
            returnMessage = cur[0] + "\n" + cur[1];
        }

        return ChatColor.translateAlternateColorCodes('&', returnMessage.replace("{SERVER}", plugin.getServerName() == null ? "" : plugin.getServerName()));
    }

    public boolean setCurrentMessage(String newName) {
        newName = newName.toLowerCase();
        if (newName.equals(currentMessage)) {
            return false;
        }

        currentMessage = newName;
        if (objectMinecraftServer != null) {
            try {
                fieldMotd.set(objectMinecraftServer, getMessage(newName));
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex.getMessage());
            }
            return true;
        }
        return true;
    }

    public Collection<String> getMessageNames() {
        return Collections.unmodifiableCollection(messages.keySet());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerListPing(ServerListPingEvent e) {
        if (objectMinecraftServer == null) {
            e.setMotd(getMessage(currentMessage));
        }
    }

}