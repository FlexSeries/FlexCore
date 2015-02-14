package me.st28.flexseries.flexcore.items;

import org.bukkit.inventory.ItemStack;

/**
 * Represents a custom item type.
 */
public abstract class CustomItemType {

    /**
     * If true, allows multiple instances of the custom item to be created.<br />
     * If false, only assigns one ID to any active item of this type.
     */
    final boolean allowsMultipleInstances;

    public CustomItemType(boolean allowsMultipleInstances) {
        this.allowsMultipleInstances = allowsMultipleInstances;
    }

    public final String getIdentifier() {
        return getClass().getCanonicalName();
    }

    /**
     * @return the ItemStack that represents this item type.
     */
    public abstract ItemStack getItemStack();

}