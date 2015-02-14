package me.st28.flexseries.flexcore.items;

import me.st28.flexseries.flexcore.utils.items.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an active instance of a {@link me.st28.flexseries.flexcore.items.CustomItemType}.
 */
public final class CustomItem {

    /**
     * The unique ID for the item.
     */
    final String id;

    /**
     * The type of custom item.
     */
    final CustomItemType type;

    /**
     * Additional custom data the item may have.
     */
    final Map<String, Object> customData = new HashMap<>();

    public CustomItem(final String id, final CustomItemType type) {
        this.id = id;
        this.type = type;
    }

    public ItemStack getItemStack(int amount) {
        ItemStack item = type.getItemStack().clone();
        if (!item.hasItemMeta()) {
            item.setItemMeta(Bukkit.getItemFactory().getItemMeta(item.getType()));
        }

        StringBuilder prefix = new StringBuilder();
        for (char ch : id.toCharArray()) {
            prefix.append(ChatColor.COLOR_CHAR + ch);
        }

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(prefix.toString() + ChatColor.RESET + ItemStackUtils.getItemName(item));
        item.setAmount(amount);
        return item;
    }

}