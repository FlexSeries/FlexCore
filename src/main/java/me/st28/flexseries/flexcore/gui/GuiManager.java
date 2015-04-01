package me.st28.flexseries.flexcore.gui;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.plugin.module.FlexModule;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manges custom inventory GUIs.
 */
public final class GuiManager extends FlexModule<FlexCore> implements Listener {

    private final Map<UUID, GUI> playerGuis = new HashMap<>();

    public GuiManager(FlexCore plugin) {
        super(plugin, "guis", "Manages inventory GUIs", false);
    }

    @Override
    protected void handleDisable() {
        for (GUI gui : playerGuis.values()) {
            gui.destroy();
        }
    }

    /**
     * @return a player's current GUI.
     */
    public GUI getGui(Player player) {
        Validate.notNull(player, "Player cannot be null.");

        return playerGuis.get(player.getUniqueId());
    }

    /**
     * Sets the GUI for a player.
     *
     * @param player The player to set the GUI for.
     * @param gui The GUI to set for the player.<br />
     *            Null to close the player's current GUI.
     */
    public void setGui(Player player, GUI gui) {
        Validate.notNull(player, "Player cannot be null.");

        UUID uuid = player.getUniqueId();

        if (playerGuis.containsKey(uuid)) {
            playerGuis.remove(uuid).closeForPlayer(player);
        }

        if (gui == null) {
            return;
        }

        playerGuis.put(uuid, gui);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent e) {
        UUID uuid = e.getWhoClicked().getUniqueId();
        GUI gui = playerGuis.get(uuid);
        if (gui == null) return;

        gui.inventoryClick(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent e) {
        UUID uuid = e.getWhoClicked().getUniqueId();
        GUI gui = playerGuis.get(uuid);
        if (gui == null) return;

        e.setCancelled(true);
    }

}