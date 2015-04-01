package me.st28.flexseries.flexcore.gui;

import org.bukkit.inventory.Inventory;

import java.util.List;

/**
 * Represents a page of a GUI.
 */
public class GuiPage {

    List<GuiItem> items;
    Inventory inventory;

    private boolean keepIfEmpty;

    /**
     * @param keepIfEmpty True to preserve the page if the GUI page is changed and this page is empty.
     */
    public GuiPage(boolean keepIfEmpty) {
        this.keepIfEmpty = keepIfEmpty;
    }

}