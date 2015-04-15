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
package me.st28.flexseries.flexcore.command;

import me.st28.flexseries.flexcore.list.ListBuilder;
import me.st28.flexseries.flexcore.message.ReplacementMap;
import me.st28.flexseries.flexcore.permission.PermissionNode;
import me.st28.flexseries.flexcore.permission.SimplePermissionNode;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.Collections;
import java.util.Map;

public final class FlexHelpCommand<T extends FlexPlugin> extends FlexSubcommand<T> {

    public FlexHelpCommand(FlexCommand<T> parent) {
        super(parent, "help", Collections.singletonList(new CommandArgument("page", false)), new FlexCommandSettings().description("Views command help"));

        PermissionNode parentPerm = parent.getPermission();
        if (parentPerm != null) {
            String helpPerm = parentPerm.getNode() + ".help";
            try {
                Bukkit.getPluginManager().addPermission(new Permission(helpPerm, PermissionDefault.TRUE));
            } catch (IllegalArgumentException ex) {}

            getSettings().permission(new SimplePermissionNode(helpPerm));
        }
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        FlexCommand<T> parent = getParent();
        Map<String, FlexSubcommand<T>> subcommands = parent.getSubcommands();

        int page = CommandUtils.getPage(args, 0);

        ListBuilder builder = new ListBuilder("page_subtitle", "Help", parent.getLabels().get(0), label);

        if (!parent.getSettings().isDummyCommand()) {
            if (parent.getPermission() != null && parent.getPermission().isAllowed(sender)) {
                String description = parent.getSettings().getDescription();
                if (description == null) {
                    //TODO: Make this configurable
                    description = "" + ChatColor.RED + ChatColor.ITALIC + "No description set.";
                }
                builder.addMessage("command", new ReplacementMap("{COMMAND}", parent.buildUsage(sender)).put("{DESCRIPTION}", description).getMap());
            }
        }

        for (FlexSubcommand<T> subcommand : subcommands.values()) {
            if (subcommand.getPermission() != null && !subcommand.getPermission().isAllowed(sender)) {
                continue;
            }

            String description = subcommand.getSettings().getDescription();
            if (description == null) {
                //TODO: Make this configurable
                description = "" + ChatColor.RED + ChatColor.ITALIC + "No description set.";
            }
            builder.addMessage("command", new ReplacementMap("{COMMAND}", subcommand.buildUsage(sender)).put("{DESCRIPTION}", description).getMap());
        }

        builder.sendTo(sender, page);
    }

}