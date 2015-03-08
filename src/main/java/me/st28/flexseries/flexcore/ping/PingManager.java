package me.st28.flexseries.flexcore.ping;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.hooks.HookManager;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import me.st28.flexseries.flexcore.utils.InternalUtils;
import me.st28.flexseries.flexcore.utils.StringConverter;
import me.st28.flexseries.flexcore.utils.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;
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
    }

    @Override
    protected void handleReload() {
        FileConfiguration config = getConfig();

        noMessage = config.getString("none set", "&c{SERVER} &8- &4&oNo message set.");

        ConfigurationSection msgSec = config.getConfigurationSection("Messages");
        messages.clear();
        if (msgSec != null) {
            for (String key : msgSec.getKeys(false)) {
                messages.put(key.toLowerCase(), StringUtils.collectionToStringList(msgSec.getStringList(key), new StringConverter<String>() {
                    @Override
                    public String toString(String object) {
                        return StringEscapeUtils.unescapeJava(object);
                    }
                }).toArray(new String[2]));
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