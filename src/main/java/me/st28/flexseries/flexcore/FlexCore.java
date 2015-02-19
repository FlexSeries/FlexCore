package me.st28.flexseries.flexcore;

import com.comphenix.packetwrapper.WrapperPlayServerChat;
import com.comphenix.protocol.PacketType.Play;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.gson.JsonParser;
import me.st28.flexseries.flexcore.commands.*;
import me.st28.flexseries.flexcore.commands.debug.CmdDebug;
import me.st28.flexseries.flexcore.commands.motd.CmdMotd;
import me.st28.flexseries.flexcore.commands.ping.CmdPing;
import me.st28.flexseries.flexcore.commands.terms.CmdTerms;
import me.st28.flexseries.flexcore.cookies.CookieManager;
import me.st28.flexseries.flexcore.debug.ArgumentDebugTest;
import me.st28.flexseries.flexcore.debug.DebugManager;
import me.st28.flexseries.flexcore.debug.MCMLDebugTest;
import me.st28.flexseries.flexcore.help.HelpManager;
import me.st28.flexseries.flexcore.hooks.HookManager;
import me.st28.flexseries.flexcore.hooks.ProtocolLibHook;
import me.st28.flexseries.flexcore.hooks.exceptions.HookDisabledException;
import me.st28.flexseries.flexcore.items.CustomItemManager;
import me.st28.flexseries.flexcore.lists.ListManager;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.messages.MessageManager;
import me.st28.flexseries.flexcore.motd.MotdManager;
import me.st28.flexseries.flexcore.ping.PingManager;
import me.st28.flexseries.flexcore.players.PlayerManager;
import me.st28.flexseries.flexcore.players.PlayerUUIDTracker;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.plugins.exceptions.ModuleDisabledException;
import me.st28.flexseries.flexcore.storage.mysql.MySQLManager;
import me.st28.flexseries.flexcore.terms.TermsManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FlexCore extends FlexPlugin implements Listener {

    public final static Pattern CHARACTER_REGEX = Pattern.compile("\\\\(\\\\u[A-F0-9]{4})");

    public static FlexCore instance;

    public static FlexCore getInstance() {
        return instance;
    }

    private String serverName;

    @Override
    public void handlePluginLoad() {
        instance = this;

        registerModule(new CookieManager(this));
        registerModule(new CustomItemManager(this));
        registerModule(new DebugManager(this));
        registerModule(new HelpManager(this));
        registerModule(new HookManager(this));
        registerModule(new ListManager(this));
        registerModule(new MessageManager(this));
        registerModule(new MotdManager(this));
        registerModule(new MySQLManager(this));
        registerModule(new PingManager(this));
        registerModule(new PlayerManager(this));
        registerModule(new PlayerUUIDTracker(this));
        registerModule(new TermsManager(this));
    }

    @Override
    public void handlePluginEnable() {
        //TODO: Register commands automatically.

        FlexCommandWrapper.registerCommand(this, "flexdebug", new CmdDebug(this));
        FlexCommandWrapper.registerCommand(this, "flexhooks", new CmdHooks(this));
        FlexCommandWrapper.registerCommand(this, "flexmodules", new CmdModules(this));
        FlexCommandWrapper.registerCommand(this, "flexmotd", new CmdMotd(this));
        FlexCommandWrapper.registerCommand(this, "flexping", new CmdPing(this));
        FlexCommandWrapper.registerCommand(this, "flexreload", new CmdReload(this));
        FlexCommandWrapper.registerCommand(this, "flexsave", new CmdSave(this));
        FlexCommandWrapper.registerCommand(this, "flexterms", new CmdTerms(this));

        try {
            DebugManager debugManager = FlexPlugin.getRegisteredModule(DebugManager.class);

            debugManager.registerDebugTest(new ArgumentDebugTest(this));
            debugManager.registerDebugTest(new MCMLDebugTest(this));
        } catch (ModuleDisabledException ex) {
            LogHelper.warning(this, "Unable to register default debug tests because the debug manager is not enabled.");
        }

        try {
            if (getConfig().getBoolean("Enable Character Fix", true)) {
                FlexPlugin.getRegisteredModule(HookManager.class).getHook(ProtocolLibHook.class).getProtocolManager().addPacketListener(new PacketAdapter(this, Play.Server.CHAT) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        WrapperPlayServerChat wrapper = new WrapperPlayServerChat(event.getPacket());

                        String fixedJson = new JsonParser().parse(wrapper.getMessage().getJson()).getAsJsonObject().toString();
                        Matcher matcher = CHARACTER_REGEX.matcher(fixedJson);
                        while (matcher.find()) {
                            fixedJson = fixedJson.replace(matcher.group(), matcher.group(1));
                        }

                        wrapper.setMessage(WrappedChatComponent.fromJson(fixedJson));
                    }
                });
                LogHelper.info(this, "Character fix enabled.");
            } else {
                LogHelper.info(this, "Character fix disabled.");
            }
        } catch (ModuleDisabledException ex) {
            LogHelper.warning(this, "Character fix is enabled in the configuration but the hook manager is not enabled.");
        } catch (HookDisabledException ex) {
            LogHelper.warning(this, "Character fix is enabled in the configuration but ProtocolLib is not installed on the server.");
        }
    }

    @Override
    public void handleConfigReload(FileConfiguration config) {
        serverName = config.getString("Server Name", "Minecraft Server");
    }

    @Override
    public void handlePluginDisable() {
        instance = null;
    }

    public String getServerName() {
        return serverName;
    }

}