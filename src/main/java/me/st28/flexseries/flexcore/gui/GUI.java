package me.st28.flexseries.flexcore.gui;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

/**
 * Represents a custom inventory GUI.
 */
public class GUI {

    private String title;
    private int size;

    private Inventory inventory;

    /**
     * Creates a GUI.
     *
     * @param title The title to display in the inventory.
     * @param size The number of rows that the inventory should contain.
     */
    public GUI(String title, int size) {
        Validate.notNull(title, "Title cannot be null.");
        Validate.isTrue(title.length() <= 32, "Title cannot be longer than 32 characters");
        Validate.isTrue(size >= 1 && size <= 9, "Size must be 1-9");

        this.title = title;
        this.size = size;

        inventory = Bukkit.createInventory(null, size * 9, ChatColor.translateAlternateColorCodes('&', title));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        Validate.notNull(title, "Title cannot be null.");
        throw new UnsupportedOperationException("Currently not implemented.");
        //this.title = title;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        Validate.isTrue(size >= 1 && size <= 9, "Size must be 1-9");
        throw new UnsupportedOperationException("Currently not implemented.");
        //this.size = size;
    }

    public Inventory getInventory() {
        return inventory;
    }

}