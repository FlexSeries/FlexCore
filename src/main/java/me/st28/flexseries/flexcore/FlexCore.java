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
package me.st28.flexseries.flexcore;

import me.st28.flexseries.flexcore.backend.commands.CmdHooks;
import me.st28.flexseries.flexcore.backend.commands.CmdModules;
import me.st28.flexseries.flexcore.backend.commands.CmdReload;
import me.st28.flexseries.flexcore.backend.commands.CmdSave;
import me.st28.flexseries.flexcore.backend.commands.debug.CmdDebug;
import me.st28.flexseries.flexcore.backend.debug.tests.*;
import me.st28.flexseries.flexcore.command.FlexCommandWrapper;
import me.st28.flexseries.flexcore.cookie.CookieManager;
import me.st28.flexseries.flexcore.debug.DebugManager;
import me.st28.flexseries.flexcore.gui.GuiManager;
import me.st28.flexseries.flexcore.hook.HookManager;
import me.st28.flexseries.flexcore.item.CustomItemManager;
import me.st28.flexseries.flexcore.list.ListManager;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.message.MessageManager;
import me.st28.flexseries.flexcore.player.PlayerManager;
import me.st28.flexseries.flexcore.player.uuid_tracker.PlayerUuidTracker;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import me.st28.flexseries.flexcore.plugin.exceptions.ModuleDisabledException;
import me.st28.flexseries.flexcore.storage.mysql.MySQLManager;
import me.st28.flexseries.flexcore.variable.MessageVariable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
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
        // Register commands
        FlexCommandWrapper.registerCommand(this, new CmdDebug(this));
        FlexCommandWrapper.registerCommand(this, new CmdHooks(this));
        FlexCommandWrapper.registerCommand(this, new CmdModules(this));
        FlexCommandWrapper.registerCommand(this, new CmdReload(this));
        FlexCommandWrapper.registerCommand(this, new CmdSave(this));

        // Register debug tests
        try {
            DebugManager debugManager = FlexPlugin.getRegisteredModule(DebugManager.class);

            debugManager.registerDebugTest(new ActionBarDebugTest(this));
            debugManager.registerDebugTest(new ArgumentDebugTest(this));
            debugManager.registerDebugTest(new CustomItemDebugTest(this));
            debugManager.registerDebugTest(new GuiDebugTest(this));
            debugManager.registerDebugTest(new MCMLDebugTest(this));
        } catch (ModuleDisabledException ex) {
            LogHelper.warning(this, "Unable to register default debug tests because the debug manager is not enabled.");
        }

        // Register default variables
        MessageVariable.registerVariable(new MessageVariable("server") {
            @Override
            public String getReplacement(Player player) {
                return serverName;
            }
        });

        MessageVariable.registerVariable(new MessageVariable("name") {
            @Override
            public String getReplacement(Player player) {
                if (player == null) return null;
                return player.getName();
            }
        });

        MessageVariable.registerVariable(new MessageVariable("dispname") {
            @Override
            public String getReplacement(Player player) {
                if (player == null) return null;
                return player.getDisplayName();
            }
        });

        MessageVariable.registerVariable(new MessageVariable("world") {
            @Override
            public String getReplacement(Player player) {
                if (player == null) return null;
                return player.getWorld().getName();
            }
        });
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