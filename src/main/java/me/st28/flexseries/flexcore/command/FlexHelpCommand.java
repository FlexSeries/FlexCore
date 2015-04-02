package me.st28.flexseries.flexcore.command;

import me.st28.flexseries.flexcore.list.ListBuilder;
import me.st28.flexseries.flexcore.message.ReplacementMap;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.Map;

public final class FlexHelpCommand<T extends FlexPlugin> extends FlexSubcommand<T> {

    public FlexHelpCommand(FlexCommand<T> parent) {
        super(parent, "help", Collections.singletonList(new CommandArgument("page", false)), new FlexCommandSettings().description("Views command help"));

        //TODO: Create help permission node
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