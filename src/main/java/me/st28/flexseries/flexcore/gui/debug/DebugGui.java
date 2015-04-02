/**
 * FlexCore - Licensed under the MIT License (MIT)
 *
 * Copyright (c) Stealth2800 <http://stealthyone.com/>
 * Copyright (c) contributors <https://github.com/FlexSeries>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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