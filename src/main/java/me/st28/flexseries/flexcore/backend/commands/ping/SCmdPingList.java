package me.st28.flexseries.flexcore.backend.commands.ping;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.lists.ListBuilder;
import me.st28.flexseries.flexcore.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.ping.PingManager;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Map;

public final class SCmdPingList extends FlexCommand<FlexCore> {

    public SCmdPingList(FlexCore plugin, FlexCommand<FlexCore> parent) {
        super(plugin,
                new String[]{"list"},
                parent,
                new FlexCommandSettings<FlexCore>()
                        .permission(PermissionNodes.PING_LIST)
                        .description("Lists available server list messages"),
                new CommandArgument("page", false)
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        PingManager pingManager = FlexPlugin.getRegisteredModule(PingManager.class);

        String currentMessage = pingManager.getCurrentMessageName();
        Collection<String> messages = pingManager.getMessageNames();

        StringBuilder list = new StringBuilder();
        for (String key : messages) {
            if (list.length() > 0) {
                list.append(ChatColor.DARK_GRAY).append(", ");
            }
            list.append(key.equalsIgnoreCase(currentMessage) ? ChatColor.GREEN : ChatColor.RED).append(key);
        }

        ListBuilder builder = new ListBuilder("title", "Server List Messages", null, label);
        if (list.length() > 0) {
            builder.addMessage(list.toString());
        }
        builder.sendTo(sender, 1);
    }

}