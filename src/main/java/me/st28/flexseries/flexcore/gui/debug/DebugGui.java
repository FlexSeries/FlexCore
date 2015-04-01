package me.st28.flexseries.flexcore.gui.debug;

import me.st28.flexseries.flexcore.gui.GUI;
import me.st28.flexseries.flexcore.gui.GuiItem;
import me.st28.flexseries.flexcore.gui.GuiPage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class DebugGui extends GUI {

    public DebugGui() {
        super("&4&lDebug GUI", 6, 0, true, true, true, true);

        addPage(new GuiPage(true));
        addPage(new GuiPage(true));

        setItem(0, 0, 0, new GuiItem() {
            @Override
            public ItemStack getItemStack() {
                return new ItemStack(Material.GRASS);
            }

            @Override
            public void handleClick(Player player, ClickType clickType) {
                player.sendMessage("Click: " + clickType.name());
            }
        });

        setItem(0, 8, 0, new GuiItem() {
            @Override
            public ItemStack getItemStack() {
                return new ItemStack(Material.GRASS);
            }

            @Override
            public void handleClick(Player player, ClickType clickType) {
                player.sendMessage("Click: " + clickType.name());
            }
        });

        setItem(0, 0, 5, new GuiItem() {
            @Override
            public ItemStack getItemStack() {
                return new ItemStack(Material.GRASS);
            }

            @Override
            public void handleClick(Player player, ClickType clickType) {
                player.sendMessage("Click: " + clickType.name());
            }
        });

        setItem(0, 8, 5, new GuiItem() {
            @Override
            public ItemStack getItemStack() {
                return new ItemStack(Material.EMERALD_BLOCK);
            }

            @Override
            public void handleClick(Player player, ClickType clickType) {
                player.sendMessage("Next Page");
                nextPlayerPage(player);
            }
        });

        setItem(1, 0, 5, new GuiItem() {
            @Override
            public ItemStack getItemStack() {
                return new ItemStack(Material.REDSTONE_BLOCK);
            }

            @Override
            public void handleClick(Player player, ClickType clickType) {
                player.sendMessage("Prev page");
                previousPlayerPage(player);
            }
        });
    }

}