package me.st28.flexseries.flexcore.item;

import me.st28.flexseries.flexcore.util.item.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an active instance of a {@link CustomItemType}.
 */
public final class CustomItem {

    /**
     * The IID for this custom item.
     */
    final String iid;

    /**
     * The type of custom item.
     */
    final CustomItemType type;

    /**
     * Custom data for this item that only exists in memory.
     */
    final Map<String, Object> temporaryCustomData = new HashMap<>();

    /**
     * Custom data for this item that is saved and loaded to the disk.<br />
     * Values should be ConfigurationSerializable if they are not supported by default.
     */
    final Map<String, Object> persistentCustomData = new HashMap<>();

    CustomItem(final String iid, final CustomItemType type) {
        this.iid = iid;
        this.type = type;
    }

    /**
     * @return the temporary custom data for this item.
     */
    public final Map<String, Object> getTemporaryCustomData() {
        return temporaryCustomData;
    }

    /**
     * @return the persistent custom data for this item.
     */
    public final Map<String, Object> getPersistentCustomData() {
        return persistentCustomData;
    }

    /**
     * @return Builds an ItemStack that represents this custom item.
     */
    public final ItemStack getItemStack(int amount) {
        ItemStack item = type.getItemStack(this).clone();
        if (!item.hasItemMeta()) {
            item.setItemMeta(Bukkit.getItemFactory().getItemMeta(item.getType()));
        }

        StringBuilder prefix = new StringBuilder();
        for (char ch : iid.toCharArray()) {
            prefix.append(ChatColor.COLOR_CHAR + ch);
        }

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(prefix.toString() + ChatColor.RESET + ItemStackUtils.getItemName(item));
        item.setAmount(amount);
        return item;
    }

}