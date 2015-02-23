package me.st28.flexseries.flexcore.commands.motd;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.motd.MotdManager;
import me.st28.flexseries.flexcore.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.utils.QuickMap;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Map;

public final class SCmdMotdSet extends FlexCommand<FlexCore> {

    public SCmdMotdSet(FlexCore plugin, CmdMotd parent) {
        super(
                plugin,
                new String[]{"set"},
                parent,
                new FlexCommandSettings<FlexCore>()
                        .permission(PermissionNodes.MOTD_SET)
                        .description("Sets the current MOTD"),
                new CommandArgument("motd", true)
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        String newName = args[0];

        MotdManager messageManager = FlexPlugin.getRegisteredModule(MotdManager.class);

        Collection<String> messages = messageManager.getMessageNames();
        String currentMessage = messageManager.getCurrentMessageName();

        if (!messages.contains(newName)) {
            MessageReference.create(FlexCore.class, "motd.errors.message_unknown", new QuickMap<>("{NAME}", newName).getMap()).sendTo(sender);
            return;
        }

        if (messageManager.setCurrentMessage(newName)) {
            MessageReference.create(FlexCore.class, "motd.notices.message_set", new QuickMap<>("{MESSAGE}", currentMessage).getMap()).sendTo(sender);
        } else {
            MessageReference.create(FlexCore.class, "motd.errors.message_already_set", new QuickMap<>("{MESSAGE}", currentMessage).getMap()).sendTo(sender);
        }
    }

}