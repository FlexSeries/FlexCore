package me.st28.flexseries.flexcore.commands;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.commands.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.cookies.CookieManager;
import me.st28.flexseries.flexcore.help.HelpManager;
import me.st28.flexseries.flexcore.help.HelpTopic;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.messages.ReplacementMap;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.plugins.exceptions.ModuleDisabledException;
import me.st28.flexseries.flexcore.utils.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public final class FlexCommandWrapper implements CommandExecutor {

    /**
     * Registers a command that uses FlexCore's command library.
     *
     * @param plugin The plugin that owns the command.
     * @param name The name of the command that is being registered.
     * @param command The command to register.
     */
    public static void registerCommand(FlexPlugin plugin, String name, FlexCommand<?> command) {
        Validate.notNull(plugin, "Plugin cannot be null.");
        Validate.notNull(plugin, "Command cannot be null.");

        PluginCommand bukkitCommand = plugin.getCommand(name);
        if (bukkitCommand == null) {
            throw new IllegalArgumentException("Plugin '" + plugin.getName() + "' doesn't have the command '" + name + "' registered.");
        }

        bukkitCommand.setExecutor(new FlexCommandWrapper(command));
    }

    private final FlexCommand<?> command;

    private FlexCommandWrapper(FlexCommand<?> command) {
        this.command = command;
    }

    @Override
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            CookieManager cookieManager = FlexPlugin.getRegisteredModule(CookieManager.class);
            Player cookieOwner = sender instanceof Player ? (Player) sender : null;

            args = CommandUtils.fixArguments(args);

            FlexCommand<?> currentCommand = this.command.settings.subcommandAliases.get(label.toLowerCase());
            if (currentCommand == null) {
                currentCommand = this.command;

                cookieManager.setValue(cookieOwner, label, currentCommand.plugin.getClass(), currentCommand.getLabelCookieIdentifier());
            }

            int curIndex = 0;
            while (args.length > curIndex) {
                FlexCommand subcommand = currentCommand.getSubcommands().get(args[curIndex].toLowerCase());

                if (subcommand != null) {
                    currentCommand = subcommand;
                    cookieManager.setValue(cookieOwner, args[curIndex].toLowerCase(), subcommand.plugin.getClass(), subcommand.getLabelCookieIdentifier());
                    curIndex++;
                } else {
                    break;
                }
            }

            CommandUtils.performPlayerTest(sender, currentCommand.settings.isPlayerOnly);
            CommandUtils.performPermissionTest(sender, currentCommand.getPermissionNode());

            String[] newArgs;
            if (args.length < curIndex) {
                newArgs = args;
            } else {
                newArgs = CommandUtils.fixArguments(ArrayUtils.stringArraySublist(args, curIndex, args.length));
            }

            if (currentCommand.settings.isDummyCommand || newArgs.length < currentCommand.getRequiredArgs()) {
                String defCommand = currentCommand.settings.defaultSubcommand;
                if (defCommand == null) {
                    if (currentCommand.settings.isDummyCommand) {
                        HelpTopic topic = currentCommand.plugin.getHelpTopic();
                        if (topic != null) {
                            FlexPlugin.getRegisteredModule(HelpManager.class).buildMessages(topic.getIdentifier(), sender, newArgs.length == 0 ? label : newArgs[0], null).sendTo(sender, 1);
                        }
                    } else {
                        CommandUtils.performArgsTest(newArgs.length, currentCommand.getRequiredArgs(), MessageReference.createPlain(currentCommand.buildUsage(sender)));
                    }
                    return true;
                }

                currentCommand = currentCommand.subcommands.get(currentCommand.settings.defaultSubcommand);

                CommandUtils.performPlayerTest(sender, currentCommand.settings.isPlayerOnly);
                CommandUtils.performPermissionTest(sender, currentCommand.getPermissionNode());
                CommandUtils.performArgsTest(newArgs.length, currentCommand.getRequiredArgs(), MessageReference.createPlain(currentCommand.buildUsage(sender)));
            }

            currentCommand.runCommand(sender, "/" + label + " " + ArrayUtils.stringArrayToString(args, " "), label, newArgs);
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