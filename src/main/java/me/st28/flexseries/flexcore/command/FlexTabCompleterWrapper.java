package me.st28.flexseries.flexcore.command;

import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.util.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public final class FlexTabCompleterWrapper implements TabCompleter {

    private FlexCommand<?> command;

    public FlexTabCompleterWrapper(FlexCommand<?> command) {
        this.command = command;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        try {
            args = CommandUtils.fixArguments(args);
            FlexCommand<?> currentCommand = this.command.getSubcommands().get(label.toLowerCase());;

            if (currentCommand == null) {
                currentCommand = this.command;
            }

            int curIndex = 0;
            while (args.length > curIndex) {
                FlexCommand subcommand = currentCommand.getSubcommands().get(args[curIndex].toLowerCase());

                if (subcommand != null) {
                    currentCommand = subcommand;
                    curIndex++;
                } else {
                    break;
                }
            }

            FlexCommandSettings commandSettings = currentCommand.getSettings();

            CommandUtils.performPlayerTest(sender, commandSettings.isPlayerOnly());
            CommandUtils.performPermissionTest(sender, commandSettings.getPermission());

            String[] fullNewArgs;
            if (args.length < curIndex) {
                fullNewArgs = args;
            } else {
                fullNewArgs = CommandUtils.fixArguments(ArrayUtils.stringArraySublist(args, curIndex, args.length));
            }

            List<String> newArgs = new ArrayList<>();
            for (String newArg : fullNewArgs) {
                Matcher matcher = FlexCommandWrapper.PARAMETER_PATTERN.matcher(newArg);
                if (matcher.matches()) {
                    continue;
                } else {
                    matcher = FlexCommandWrapper.PARAMETER_VALUE_PATTERN.matcher(newArg);
                    if (matcher.matches()) {
                        continue;
                    }
                }

                newArgs.add(newArg);
            }

            if (commandSettings.isDummyCommand() || newArgs.size() < currentCommand.getRequiredArguments()) {
                String defCommand = commandSettings.getDefaultSubcommand();
                if (defCommand != null) {
                    currentCommand = currentCommand.getSubcommands().get(commandSettings.getDefaultSubcommand());

                    CommandUtils.performPlayerTest(sender, commandSettings.isPlayerOnly());
                    CommandUtils.performPermissionTest(sender, commandSettings.getPermission());
                    CommandUtils.performArgsTest(newArgs.size(), currentCommand.getRequiredArguments(), MessageReference.createPlain(currentCommand.buildUsage(sender)));
                }
            }

            if (currentCommand instanceof FlexTabCompleter) {
                return ((FlexTabCompleter) currentCommand).getTabOptions(sender, newArgs.toArray(new String[newArgs.size()]));
            }
        } catch (Exception ex) {}
        return null;
    }

}