package me.st28.flexseries.flexcore.motd;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.events.PlayerJoinLoadedEvent;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.players.PlayerManager;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import me.st28.flexseries.flexcore.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public final class MotdManager extends FlexModule<FlexCore> implements Listener {

    private String currentMessage = "default";
    private Map<String, String[]> messages = new LinkedHashMap<>();

    public MotdManager(FlexCore plugin) {
        super(plugin, "motd", "Manages available MOTDs", PlayerManager.class);
    }

    @Override
    public void handleReload() {
        FileConfiguration config = getConfig();

        currentMessage = config.getString("Current", "default").toLowerCase();
        ConfigurationSection msgSec = config.getConfigurationSection("Messages");
        messages.clear();
        if (msgSec != null) {
            for (String key : msgSec.getKeys(false)) {
                messages.put(key.toLowerCase(), msgSec.getStringList(key).toArray(new String[2]));
            }
        }
    }

    @Override
    public void handleSave(boolean async) {
        getConfig().set("Current", currentMessage);
    }

    public String getCurrentMessageName() {
        return currentMessage;
    }

    public boolean setCurrentMessage(String newName) {
        newName = newName.toLowerCase();
        if (newName.equals(currentMessage)) {
            return false;
        }

        currentMessage = newName;
        return true;
    }

    public Collection<String> getMessageNames() {
        return Collections.unmodifiableCollection(messages.keySet());
    }

    @EventHandler
    public void onPlayerJoinLoaded(PlayerJoinLoadedEvent e) {
        String[] cur = messages.get(currentMessage);
        if (cur == null) return;

        List<String> toSend = new ArrayList<>();
        for (String s : cur) {
            toSend.add(ChatColor.translateAlternateColorCodes('&', s));
        }

        e.addLoginMessage(FlexCore.class, "motd", MessageReference.createPlain(StringUtils.stringCollectionToString(toSend, "\n")));
    }

}