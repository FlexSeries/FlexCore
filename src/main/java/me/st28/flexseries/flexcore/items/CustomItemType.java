package me.st28.flexseries.flexcore.items;

import org.bukkit.inventory.ItemStack;

/**
 * Represents a custom item type.
 */
public abstract class CustomItemType {

    /**
     * If true, allows multiple instances of this custom item to be created.<br />
     * If false, only assigns one ID to any custom item of this type.
     */
    final boolean isSingleton;

    public CustomItemType(boolean isSingleton) {
        this.isSingleton = isSingleton;
    }

    /**
     * @return a unique identifier for this particular item type.
     */
    public final String getIdentifier() {
        return getClass().getCanonicalName();
    }

    /**
     * @param item An active instance of this item type.
     *
     * @return the ItemStack that represents this item type.
     */
    public abstract ItemStack getItemStack(CustomItem item);

}