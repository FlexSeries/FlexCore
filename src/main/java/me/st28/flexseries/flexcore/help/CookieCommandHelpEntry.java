package me.st28.flexseries.flexcore.help;

import me.st28.flexseries.flexcore.commands.FlexCommand;
import org.bukkit.command.CommandSender;

public final class CookieCommandHelpEntry extends HelpEntry {

    private FlexCommand command;

    public CookieCommandHelpEntry(FlexCommand command) {
        super("command");
        this.command = command;
    }

    @Override
    String getMessage(CommandSender sender) {
        return command.buildUsage(sender);
    }

}