package me.st28.flexseries.flexcore.util.item;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public final class ItemStackUtils {

    private ItemStackUtils() { }

    public static String getMaterialName(Material material) {
        return WordUtils.capitalize(material.name().toLowerCase().replace("_", " "));
    }

    public static String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return getMaterialName(item.getType());
    }

    public static ItemStack getPlayerHead(String name) {
        ItemStack returnItem = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta newMeta = (SkullMeta) returnItem.getItemMeta();
        newMeta.setOwner(name);
        returnItem.setItemMeta(newMeta);
        return returnItem;
    }

}