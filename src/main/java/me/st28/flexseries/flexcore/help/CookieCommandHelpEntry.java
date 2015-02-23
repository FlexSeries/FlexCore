package me.st28.flexseries.flexcore.help;

import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.utils.StringUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CookieCommandHelpEntry extends HelpEntry {

    private FlexCommand<?> command;

    public CookieCommandHelpEntry(FlexCommand command) {
        super("command");
        this.command = command;
    }

    @Override
    String getMessage(CommandSender sender) {
        String[] rawSplit = command.buildUsage(sender).split(" ", 2);
        String label = rawSplit[0].replace("/" , "");
        String args = rawSplit.length > 1 ? rawSplit[1] : "";

        List<String> aliases = new ArrayList<>();
        if (command.getParent() != null) {
            Collections.addAll(aliases, command.getParents().get(0).getLabels());
        } else {
            Collections.addAll(aliases, command.getLabels());
        }
        aliases.remove(label);

        String description = command.getSettings().getDescription();
        if (description == null) {
            //TODO: Make this non-hardcoded
            description = "&c&oNo description set.";
        }

        return getFormat()
                .replace("{LABEL}", label)
                .replace("{COMMAND}", (args != null ? " " : "") + args)
                .replace("{DESCRIPTION}", description)
                .replace("{ALIASES}", StringUtils.stringCollectionToString(aliases, ", "));
    }

}