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
import me.st28.flexseries.flexcore.list.ListBuilder;
import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import me.st28.flexseries.flexcore.util.PluginUtils;
import me.st28.flexseries.flexcore.util.QuickMap;
import me.st28.flexseries.flexcore.util.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.Map.Entry;

public final class SCmdDebugList extends FlexSubcommand<FlexCore> implements FlexTabCompleter {

    public SCmdDebugList(CmdDebug cmdDebug) {
        super(cmdDebug, "list", Arrays.asList(new CommandArgument("plugin", false), new CommandArgument("page", false)), new FlexCommandSettings<FlexCore>().description("Lists debug tests"));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        boolean pageArgFirst = true;

        int page = CommandUtils.getPage(args, 0, true);
        if (page == -1) {
            pageArgFirst = false;
            page = CommandUtils.getPage(args, 1);
        }

        Map<Class<? extends JavaPlugin>, Map<String, DebugTest>> debugTests = FlexPlugin.getRegisteredModule(DebugManager.class).getDebugTests();

        if (pageArgFirst) {
            ListBuilder builder = new ListBuilder("title", "Debug Tests", null, label);

            if (!debugTests.isEmpty()) {
                List<String> pluginNames = new ArrayList<>();
                for (Class<? extends JavaPlugin> pluginClass : debugTests.keySet()) {
                    pluginNames.add(JavaPlugin.getPlugin(pluginClass).getName());
                }

                builder.addMessage(StringUtils.stringCollectionToString(pluginNames, ", "));
            }

            builder.sendTo(sender, 1);
        } else {
            JavaPlugin plugin = (JavaPlugin) PluginUtils.getPlugin(args[0]);
            if (plugin == null) {
                throw new CommandInterruptedException(MessageReference.createGeneral(FlexCore.class, "errors.plugin_not_found", new QuickMap<>("{NAME}", args[0]).getMap()));
            }

            Map<String, DebugTest> tests = debugTests.get(plugin.getClass());

            ListBuilder builder = new ListBuilder("page_subtitle", "Debug Tests", plugin.getName(), label);

            if (tests != null) {
                for (Entry<String, DebugTest> entry : tests.entrySet()) {
                    builder.addMessage("title", entry.getKey(), entry.getValue().getDescription());
                }
            }

            builder.sendTo(sender, page);
        }
    }

    @Override
    public List<String> getTabOptions(CommandSender sender, String[] args) {
        Map<Class<? extends JavaPlugin>, Map<String, DebugTest>> debugTests = FlexPlugin.getRegisteredModule(DebugManager.class).getDebugTests();
        List<String> returnList = new ArrayList<>();

        for (Class<? extends JavaPlugin> pluginClass : debugTests.keySet()) {
            returnList.add(JavaPlugin.getPlugin(pluginClass).getName());
        }

        return returnList;
    }

}