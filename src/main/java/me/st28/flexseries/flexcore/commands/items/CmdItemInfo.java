package me.st28.flexseries.flexcore.commands.items;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.commands.FlexHelpCommand;
import me.st28.flexseries.flexcore.commands.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.items.ItemNameIndex;
import me.st28.flexseries.flexcore.lists.ListBuilder;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.plugins.exceptions.ModuleDisabledException;
import me.st28.flexseries.flexcore.utils.MaterialRef;
import me.st28.flexseries.flexcore.utils.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CmdItemInfo extends FlexCommand<FlexCore> {

    public CmdItemInfo(FlexCore plugin) {
        super(
                plugin,
                "flexiteminfo",
                new FlexCommandSettings<FlexCore>()
                    .permission(PermissionNodes.ITEM_INFO)
                    .setPlayerOnly()
                    .helpPath("FlexCore.ItemInfo")
        );

        registerSubcommand(new FlexHelpCommand<>(plugin, this));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        ItemStack item = ((Player) sender).getItemInHand();

        if (item == null) {
            throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "errors.must_hold_item"));
        }

        MaterialRef type = new MaterialRef(item);
        List<String> aliases = new ArrayList<>();
        try {
            aliases.addAll(FlexPlugin.getRegisteredModule(ItemNameIndex.class).getAliases(type));
        } catch (ModuleDisabledException ex) { }

        ListBuilder builder = new ListBuilder("title", "Item Info", null, label);

        builder.addMessage("title", "Type", type.toString());
        if (!aliases.isEmpty()) {
            builder.addMessage("title", "Aliases", StringUtils.stringCollectionToString(aliases, ", "));
        }

        builder.sendTo(sender, 1);
    }

}