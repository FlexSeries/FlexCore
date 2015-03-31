package me.st28.flexseries.flexcore.item;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.command.CommandUtils;
import me.st28.flexseries.flexcore.debug.DebugTest;
import me.st28.flexseries.flexcore.debug.DebugTestOutput;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

public final class CustomItemDebugTest extends DebugTest<FlexCore> {

    public CustomItemDebugTest(FlexCore plugin) {
        super(plugin, "customitem", "Shows the underlying custom item ID on an item.");

        playerOnly = true;
    }

    @Override
    public DebugTestOutput runTest(CommandSender sender, String label, String[] args) {
        ItemStack handItem = CommandUtils.getSenderPlayer(sender).getItemInHand();

        if (handItem == null || handItem.getType() == Material.AIR) {
            return new DebugTestOutput(false, "Must be holding an item.");
        }

        String id = plugin.getModule(CustomItemManager.class).readId(handItem);
        if (id == null) {
            return new DebugTestOutput(false, "Held item is not a custom item.");
        }

        return new DebugTestOutput(true, "Found ID: " + id);
    }

}