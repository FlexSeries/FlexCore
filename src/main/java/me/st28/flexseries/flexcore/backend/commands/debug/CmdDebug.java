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
package me.st28.flexseries.flexcore.backend.commands.debug;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.command.*;
import me.st28.flexseries.flexcore.command.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.debug.DebugManager;
import me.st28.flexseries.flexcore.debug.DebugTest;
import me.st28.flexseries.flexcore.debug.DebugTestOutput;
import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.message.ReplacementMap;
import me.st28.flexseries.flexcore.permission.PermissionNodes;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import me.st28.flexseries.flexcore.util.ArrayUtils;
import me.st28.flexseries.flexcore.util.PluginUtils;
import me.st28.flexseries.flexcore.util.QuickMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class CmdDebug extends FlexCommand<FlexCore> implements FlexTabCompleter {

    public CmdDebug(FlexCore plugin) {
        super(plugin, "flexdebug", Arrays.asList(new CommandArgument("plugin", true), new CommandArgument("test", true)), new FlexCommandSettings().permission(PermissionNodes.DEBUG).defaultSubcommand("help"));

        registerSubcommand(new SCmdDebugList(this));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        JavaPlugin plugin = (JavaPlugin) PluginUtils.getPlugin(args[0]);
        if (plugin == null) {
            throw new CommandInterruptedException(MessageReference.createGeneral(FlexCore.class, "errors.plugin_not_found", new QuickMap<>("{NAME}", args[0]).getMap()));
        }

        DebugTest test = FlexPlugin.getRegisteredModule(DebugManager.class).getDebugTest(plugin, args[1]);
        if (test == null) {
            throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "debug.errors.unknown_test", new ReplacementMap("{PLUGIN}", args[0].toLowerCase()).put("{TEST}", args[1].toLowerCase()).getMap()));
        }

        CommandUtils.performPlayerTest(sender, test.isPlayerOnly());
        CommandUtils.performPermissionTest(sender, test.getPermission());

        String[] newArgs = ArrayUtils.stringArraySublist(args, 2, args.length);
        String usage = "/" + label + " " + ArrayUtils.stringArrayToString(ArrayUtils.stringArraySublist(args, 0, 2)) + " " + test.buildUsage();
        CommandUtils.performArgsTest(newArgs.length, test.getRequiredArgs(), MessageReference.createPlain(usage));

        try {
            DebugTestOutput output = test.runTest(sender, label, newArgs);

            MessageReference message = output.getMessage();
            if (output.isSuccess()) {
                if (message != null) {
                    MessageReference.combine(MessageReference.create(FlexCore.class, "debug.notices.test_success_output"), "{OUTPUT}", message).sendTo(sender);
                } else {
                    MessageReference.create(FlexCore.class, "debug.notices.test_success").sendTo(sender);
                }
            } else {
                if (message != null) {
                    MessageReference.combine(MessageReference.create(FlexCore.class, "debug.errors.test_failed_output"), "{OUTPUT}", message).sendTo(sender);
                } else {
                    MessageReference.create(FlexCore.class, "debug.errors.test_failed").sendTo(sender);
                }
            }
        } catch (Exception ex) {
            MessageReference.create(FlexCore.class, "debug.errors.test_exception", new ReplacementMap("{OUTPUT}", "(" + ex.getClass().getCanonicalName() + ") " + ex.getMessage()).getMap()).sendTo(sender);
            ex.printStackTrace();
        }
    }

    @Override
    public List<String> getTabOptions(CommandSender sender, String[] args) {
        Map<Class<? extends JavaPlugin>, Map<String, DebugTest>> debugTests = FlexPlugin.getRegisteredModule(DebugManager.class).getDebugTests();

        List<String> returnList = new ArrayList<>();
        if (args.length == 1) {
            for (Class<? extends JavaPlugin> pluginClass : debugTests.keySet()) {
                returnList.add(JavaPlugin.getPlugin(pluginClass).getName());
            }
        } else {
            JavaPlugin plugin = (JavaPlugin) PluginUtils.getPlugin(args[0]);
            if (plugin == null) {
                return null;
            }

            Map<String, DebugTest> tests = debugTests.get(plugin.getClass());
            returnList.addAll(tests.keySet());
        }
        return returnList;
    }

}