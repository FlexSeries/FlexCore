package me.st28.flexseries.flexcore.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an item in a GUI.
 */
public abstract class GuiItem {

    /**
     * @return the ItemStack that this GuiItem exists as in a GUI inventory.
     */
    public abstract ItemStack getItemStack();

    /**
     * @return true if the item can be moved within the inventory.
     */
    public abstract boolean isMovableWithinInventory();

    /**
     * @return true if the item can be taken outside of the GUI.
     */
    public abstract boolean isMovableOutsideInventory();

    /**
     * Handles a player clicking the item in the GUI.
     *
     * @param player The player who clicked the item.
     * @param clickType The type of click.
     * @param isShift True if shift is being held down.
     */
    public abstract void handleClick(Player player, ClickType clickType, boolean isShift);

}