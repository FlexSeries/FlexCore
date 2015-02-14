package me.st28.flexseries.flexcore.commands.motd;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.lists.ListBuilder;
import me.st28.flexseries.flexcore.motd.MotdManager;
import me.st28.flexseries.flexcore.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collection;

public final class SCmdMotdList extends FlexCommand<FlexCore> {

    public SCmdMotdList(FlexCore plugin, CmdMotd parent) {
        super(plugin,
                new String[]{"list"},
                parent,
                new FlexCommandSettings<FlexCore>()
                        .permission(PermissionNodes.MOTD_LIST)
                        .description("Lists available MOTDs"),
                new CommandArgument("page", false)
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args) {
        MotdManager motdManager = FlexPlugin.getRegisteredModule(MotdManager.class);
        String currentMessage = motdManager.getCurrentMessageName();
        Collection<String> messages = motdManager.getMessageNames();

        StringBuilder list = new StringBuilder();
        for (String key : messages) {
            if (list.length() > 0) {
                list.append(ChatColor.DARK_GRAY).append(", ");
            }
            list.append(key.equalsIgnoreCase(currentMessage) ? ChatColor.GREEN : ChatColor.RED).append(key);
        }

        ListBuilder builder = new ListBuilder("title", "MOTDs", null, label);
        if (list.length() > 0) {
            builder.addMessage(list.toString());
        }
        builder.sendTo(sender, 1);
    }

}