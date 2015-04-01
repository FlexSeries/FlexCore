package me.st28.flexseries.flexcore;

import me.st28.flexseries.flexcore.backend.commands.CmdHooks;
import me.st28.flexseries.flexcore.backend.commands.CmdModules;
import me.st28.flexseries.flexcore.backend.commands.CmdReload;
import me.st28.flexseries.flexcore.backend.commands.CmdSave;
import me.st28.flexseries.flexcore.backend.commands.debug.CmdDebug;
import me.st28.flexseries.flexcore.command.FlexCommandWrapper;
import me.st28.flexseries.flexcore.cookie.CookieManager;
import me.st28.flexseries.flexcore.backend.debug.tests.ArgumentDebugTest;
import me.st28.flexseries.flexcore.debug.DebugManager;
import me.st28.flexseries.flexcore.backend.debug.tests.MCMLDebugTest;
import me.st28.flexseries.flexcore.gui.GuiManager;
import me.st28.flexseries.flexcore.backend.debug.tests.GuiDebugTest;
import me.st28.flexseries.flexcore.hook.HookManager;
import me.st28.flexseries.flexcore.backend.debug.tests.CustomItemDebugTest;
import me.st28.flexseries.flexcore.item.CustomItemManager;
import me.st28.flexseries.flexcore.list.ListManager;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.message.MessageManager;
import me.st28.flexseries.flexcore.player.PlayerManager;
import me.st28.flexseries.flexcore.player.uuid_tracker.PlayerUuidTracker;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import me.st28.flexseries.flexcore.plugin.exceptions.ModuleDisabledException;
import me.st28.flexseries.flexcore.storage.mysql.MySQLManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

public final class FlexCore extends FlexPlugin implements Listener {

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
        registerModule(new GuiManager(this));
        registerModule(new HookManager(this));
        registerModule(new ListManager(this));
        registerModule(new MessageManager(this));
        registerModule(new MySQLManager(this));
        registerModule(new PlayerManager(this));
        registerModule(new PlayerUuidTracker(this));
    }

    @Override
    public void handlePluginEnable() {
        FlexCommandWrapper.registerCommand(this, new CmdDebug(this));
        FlexCommandWrapper.registerCommand(this, new CmdHooks(this));
        FlexCommandWrapper.registerCommand(this, new CmdModules(this));
        FlexCommandWrapper.registerCommand(this, new CmdReload(this));
        FlexCommandWrapper.registerCommand(this, new CmdSave(this));

        try {
            DebugManager debugManager = FlexPlugin.getRegisteredModule(DebugManager.class);

            debugManager.registerDebugTest(new ArgumentDebugTest(this));
            debugManager.registerDebugTest(new CustomItemDebugTest(this));
            debugManager.registerDebugTest(new GuiDebugTest(this));
            debugManager.registerDebugTest(new MCMLDebugTest(this));
        } catch (ModuleDisabledException ex) {
            LogHelper.warning(this, "Unable to register default debug tests because the debug manager is not enabled.");
        }
    }

    @Override
    public void handleConfigReload(FileConfiguration config) {
        serverName = config.getString("server name", "Minecraft Server");
    }

    @Override
    public void handlePluginDisable() {
        instance = null;
    }

    public String getServerName() {
        return serverName;
    }

}