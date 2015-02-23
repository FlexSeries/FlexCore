package me.st28.flexseries.flexcore.commands.ping;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.ping.PingManager;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.utils.QuickMap;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Map;

public final class SCmdPingSet extends FlexCommand<FlexCore> {

    public SCmdPingSet(FlexCore plugin, FlexCommand<FlexCore> parent) {
        super(
                plugin,
                new String[]{"set"},
                parent,
                new FlexCommandSettings<FlexCore>()
                        .permission(PermissionNodes.PING_SET)
                        .description("Sets the current server list message"),
                new CommandArgument("message", true)
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        String newName = args[0].toLowerCase();

        PingManager pingManager = FlexPlugin.getRegisteredModule(PingManager.class);

        String currentMessage = pingManager.getCurrentMessageName();
        Collection<String> messages = pingManager.getMessageNames();

        if (!messages.contains(newName)) {
            MessageReference.create(FlexCore.class, "ping.errors.message_unknown", new QuickMap<>("{NAME}", newName).getMap()).sendTo(sender);
            return;
        }

        if (pingManager.setCurrentMessage(newName)) {
            MessageReference.create(FlexCore.class, "ping.notices.message_set", new QuickMap<>("{MESSAGE}", newName).getMap()).sendTo(sender);
        } else {
            MessageReference.create(FlexCore.class, "ping.errors.message_already_set", new QuickMap<>("{MESSAGE}", currentMessage).getMap()).sendTo(sender);
        }
    }

}