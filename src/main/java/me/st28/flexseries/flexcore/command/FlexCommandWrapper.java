package me.st28.flexseries.flexcore.command;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.command.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.cookie.CookieManager;
import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.message.ReplacementMap;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import me.st28.flexseries.flexcore.plugin.exceptions.ModuleDisabledException;
import me.st28.flexseries.flexcore.util.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FlexCommandWrapper implements CommandExecutor {

    public final static Pattern PARAMETER_PATTERN = Pattern.compile("-([^=]+)");
    public final static Pattern PARAMETER_VALUE_PATTERN = Pattern.compile("-(.+)=(.+)");

    /**
     * Registers a command that uses FlexCore's command library.
     *
     * @param plugin The plugin that owns the command.
     * @param command The command to register.
     */
    public static <T extends FlexPlugin> void registerCommand(FlexPlugin plugin, FlexCommand<T> command) {
        Validate.notNull(plugin, "Plugin cannot be null.");
        Validate.notNull(plugin, "Command cannot be null.");

        String name = command.getLabels().get(0);

        PluginCommand bukkitCommand = plugin.getCommand(name);
        if (bukkitCommand == null) {
            throw new IllegalArgumentException("Plugin '" + plugin.getName() + "' doesn't have the command '" + name + "' registered.");
        }

        bukkitCommand.setExecutor(new FlexCommandWrapper(command));
        bukkitCommand.setTabCompleter(new FlexTabCompleterWrapper(command));

        if (/*!command.getSubcommands().isEmpty() && */!command.getSubcommands().containsKey("help")) {
            command.registerSubcommand(new FlexHelpCommand<>(command));
            if (command.getSettings().isDummyCommand()) {
                command.getSettings().defaultSubcommand("help");
            }
        }

        for (FlexSubcommand<T> subcommand : command.getSubcommands().values()) {
            if (/*!subcommand.getSubcommands().isEmpty() && */!subcommand.getSubcommands().containsKey("help")) {
                subcommand.registerSubcommand(new FlexHelpCommand<>(subcommand));
                if (subcommand.getSettings().isDummyCommand()) {
                    subcommand.getSettings().defaultSubcommand("help");
                }
            }
        }
    }

    private final FlexCommand<?> command;

    private FlexCommandWrapper(FlexCommand<?> command) {
        Validate.notNull(command, "Command cannot be null.");
        this.command = command;
    }

    @Override
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!this.command.getSettings().isLocked()) {
            this.command.getSettings().lock();
        }

        try {
            CookieManager cookieManager = FlexPlugin.getRegisteredModuleSilent(CookieManager.class);
            String cookieUserId = null;

            args = CommandUtils.fixArguments(args);
            FlexCommand<?> currentCommand = this.command.getSubcommands().get(label.toLowerCase());;

            // Set the cookie for this command label
            if (cookieManager != null) {
                cookieUserId = CookieManager.getUserIdentifier(sender);

                if (currentCommand == null) {
                    currentCommand = this.command;

                    cookieManager.setValue(cookieUserId, label, currentCommand.getPlugin().getClass(), currentCommand.getLabelCookieIdentifier());
                }
            }

            int curIndex = 0;
            while (args.length > curIndex) {
                FlexCommand subcommand = currentCommand.getSubcommands().get(args[curIndex].toLowerCase());

                if (subcommand != null) {
                    if (cookieManager != null) {
                        cookieManager.setValue(cookieUserId, args[curIndex].toLowerCase(), subcommand.getPlugin().getClass(), subcommand.getLabelCookieIdentifier());
                    }

                    currentCommand = subcommand;
                    curIndex++;
                } else {
                    break;
                }
            }

            FlexCommandSettings commandSettings = currentCommand.getSettings();

            CommandUtils.performPlayerTest(sender, commandSettings.isPlayerOnly());
            CommandUtils.performPermissionTest(sender, currentCommand.getPermission());

            String[] fullNewArgs;
            if (args.length < curIndex) {
                fullNewArgs = args;
            } else {
                fullNewArgs = CommandUtils.fixArguments(ArrayUtils.stringArraySublist(args, curIndex, args.length));
            }

            Map<String, String> parameters = new HashMap<>();

            List<String> newArgs = new ArrayList<>();
            for (String newArg : fullNewArgs) {
                Matcher matcher = PARAMETER_PATTERN.matcher(newArg);
                if (matcher.matches()) {
                    parameters.put(matcher.group(1), null);
                    continue;
                } else {
                    matcher = PARAMETER_VALUE_PATTERN.matcher(newArg);
                    if (matcher.matches()) {
                        parameters.put(matcher.group(1), matcher.group(2));
                        continue;
                    }
                }

                newArgs.add(newArg);
            }

            if (commandSettings.isDummyCommand() || newArgs.size() < currentCommand.getRequiredArguments()) {
                String defCommand = commandSettings.getDefaultSubcommand();
                if (defCommand == null) {
                    if (!commandSettings.isDummyCommand()) {
                        CommandUtils.performArgsTest(newArgs.size(), currentCommand.getRequiredArguments(), MessageReference.createPlain(currentCommand.buildUsage(sender)));
                    }
                    return true;
                }

                currentCommand = currentCommand.getSubcommands().get(commandSettings.getDefaultSubcommand());

                CommandUtils.performPlayerTest(sender, commandSettings.isPlayerOnly());
                CommandUtils.performPermissionTest(sender, currentCommand.getPermission());
                CommandUtils.performArgsTest(newArgs.size(), currentCommand.getRequiredArguments(), MessageReference.createPlain(currentCommand.buildUsage(sender)));
            }

            currentCommand.runCommand(sender, "/" + label + " " + ArrayUtils.stringArrayToString(args, " "), label, newArgs.toArray(new String[newArgs.size()]), parameters);
        } catch (CommandInterruptedException ex) {
            if (ex.getMessage() != null) {
                sender.sendMessage(ex.getMessage());
            }
        } catch (ModuleDisabledException ex) {
            if (ex.getMessage() != null) {
                MessageReference.create(FlexCore.class, "lib_plugin.errors.module_disabled", new ReplacementMap("{IDENTIFIER}", ex.getMessage()).getMap()).sendTo(sender);
            }
        }
        return true;
    }

}