package me.st28.flexseries.flexcore.backend.commands.debug;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.commands.CommandUtils;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.commands.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.debug.DebugManager;
import me.st28.flexseries.flexcore.debug.DebugTest;
import me.st28.flexseries.flexcore.debug.DebugTestOutput;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.messages.ReplacementMap;
import me.st28.flexseries.flexcore.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.utils.ArrayUtils;
import me.st28.flexseries.flexcore.utils.PluginUtils;
import me.st28.flexseries.flexcore.utils.QuickMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Map;

public final class CmdDebug extends FlexCommand<FlexCore> {

    public CmdDebug(FlexCore plugin) {
        super(plugin, "flexdebug", Arrays.asList(new CommandArgument("plugin", true), new CommandArgument("test", true)), new FlexCommandSettings().permission(PermissionNodes.DEBUG));

        registerSubcommand(new SCmdDebugList(this));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        JavaPlugin plugin = (JavaPlugin) PluginUtils.getPlugin(args[0]);
        if (plugin == null) {
            throw new CommandInterruptedException(MessageReference.createGeneral(FlexCore.class, "errors.plugin_not_found", new QuickMap<>("{NAME}", args[0]).getMap()));
        }

        DebugTest test = FlexPlugin.getRegisteredModule(DebugManager.class).getDebugTest(plugin, args[1]);
        if (test == null) {
            throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "debug.errors.unknown_test", new ReplacementMap("{PLUGIN}", args[0].toLowerCase()).put("{TEST}", args[1].toLowerCase()).getMap()));
        }

        CommandUtils.performPlayerTest(sender, test.isPlayerOnly());
        CommandUtils.performPermissionTest(sender, test.getPermission());

        String[] newArgs = ArrayUtils.stringArraySublist(args, 2, args.length);
        String usage = "/" + label + " " + ArrayUtils.stringArrayToString(ArrayUtils.stringArraySublist(args, 0, 2)) + " " + test.buildUsage();
        CommandUtils.performArgsTest(newArgs.length, test.getRequiredArgs(), MessageReference.createPlain(usage));

        try {
            DebugTestOutput output = test.runTest(sender, label, newArgs);

            MessageReference message = output.getMessage();
            if (output.isSuccess()) {
                if (message != null) {
                    MessageReference.combine(MessageReference.create(FlexCore.class, "debug.notices.test_success_output"), "{OUTPUT}", message).sendTo(sender);
                } else {
                    MessageReference.create(FlexCore.class, "debug.notices.test_success").sendTo(sender);
                }
            } else {
                if (message != null) {
                    MessageReference.combine(MessageReference.create(FlexCore.class, "debug.errors.test_failed_output"), "{OUTPUT}", message).sendTo(sender);
                } else {
                    MessageReference.create(FlexCore.class, "debug.errors.test_failed").sendTo(sender);
                }
            }
        } catch (Exception ex) {
            MessageReference.create(FlexCore.class, "debug.errors.test_exception", new ReplacementMap("{OUTPUT}", ex.getMessage()).getMap()).sendTo(sender);
            ex.printStackTrace();
        }
    }

}