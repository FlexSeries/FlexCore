package me.st28.flexseries.flexcore.command;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Handles the tab completion options for a {@link FlexCommand}.
 */
public interface FlexTabCompleter {

    /**
     * @param sender The sender of the command.
     * @param args The arguments given by the sender.
     * @return A list of options that the sender can choose from.
     */
    List<String> getTabOptions(CommandSender sender, String[] args);

}