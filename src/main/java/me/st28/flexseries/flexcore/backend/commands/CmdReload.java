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
package me.st28.flexseries.flexcore.backend.commands;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.command.CommandArgument;
import me.st28.flexseries.flexcore.command.FlexCommand;
import me.st28.flexseries.flexcore.command.FlexCommandSettings;
import me.st28.flexseries.flexcore.command.FlexTabCompleter;
import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.message.ReplacementMap;
import me.st28.flexseries.flexcore.permission.PermissionNodes;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import me.st28.flexseries.flexcore.util.PluginUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class CmdReload extends FlexCommand<FlexCore> implements FlexTabCompleter {

    public CmdReload(FlexCore plugin) {
        super(plugin, "flexreload", Collections.singletonList(new CommandArgument("plugin", true)), new FlexCommandSettings().permission(PermissionNodes.RELOAD));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        String properName = PluginUtils.getProperPluginName(args[0]);
        if (properName == null) {
            MessageReference.create(FlexCore.class, "general.errors.plugin_not_found", new ReplacementMap("{NAME}", args[0]).getMap()).sendTo(sender);
            return;
        }

        Plugin targetPlugin = Bukkit.getPluginManager().getPlugin(properName);
        if (!(targetPlugin instanceof FlexPlugin)) {
            MessageReference.create(FlexCore.class, "lib_astplugin.errors.plugin_not_astplugin", new ReplacementMap("{NAME}", args[0]).getMap()).sendTo(sender);
            return;
        }

        ((FlexPlugin) targetPlugin).reloadAll();
        MessageReference.createGeneral((JavaPlugin) targetPlugin, "lib_plugin.notices.plugin_reloaded").sendTo(sender);
    }

    @Override
    public List<String> getTabOptions(CommandSender sender, String[] args) {
        List<String> returnList = new ArrayList<>();

        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin instanceof FlexPlugin) {
                returnList.add(plugin.getName());
            }
        }

        return returnList;
    }

}