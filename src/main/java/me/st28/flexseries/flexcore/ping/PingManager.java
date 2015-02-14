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
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PingManager extends FlexModule<FlexCore> implements Listener {

    private String currentMessage = "default";
    private Map<String, String[]> messages = new LinkedHashMap<>();

    public PingManager(FlexCore plugin) {
        super(plugin, "ping", "Dynamic server list message", HookManager.class);
    }

    @Override
    protected void handleLoad() throws Exception {
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
    protected void handleSave(boolean async) {
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerListPing(ServerListPingEvent e) {
        String[] cur = messages.get(currentMessage);
        if (cur == null) {
            //TODO: Customizable server name
            e.setMotd(ChatColor.RED + "Urban Astorea" + ChatColor.DARK_GRAY + " - " + ChatColor.DARK_RED + ChatColor.ITALIC + "No message set");
            return;
        }

        e.setMotd(ChatColor.translateAlternateColorCodes('&', cur[0] + "\n" + cur[1]));
    }

}