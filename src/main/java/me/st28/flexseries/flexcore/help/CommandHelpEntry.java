package me.st28.flexseries.flexcore.help;

import me.st28.flexseries.flexcore.utils.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public final class CommandHelpEntry extends HelpEntry {

    String command;
    String description;
    List<String> aliases;

    public CommandHelpEntry(String command, String description) {
        this(command, description, null, description);
    }

    public CommandHelpEntry(String command, String description, String permission) {
        this(command, description, null, permission);
    }

    public CommandHelpEntry(String command, String description, List<String> aliases, String permission) {
        super("command");
        this.command = command;
        this.description = description;
        this.aliases = aliases == null ? null : aliases;
        this.permission = permission;
    }

    @Override
    String getMessage(CommandSender sender) {
        return getFormat()
                .replace("{COMMAND}", command)
                .replace("{DESCRIPTION}", description)
                .replace("{ALIASES}", StringUtils.stringCollectionToString(aliases, ", "));
    }

}