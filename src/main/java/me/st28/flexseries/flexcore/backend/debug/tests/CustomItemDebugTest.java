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
package me.st28.flexseries.flexcore.backend.debug.tests;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.command.CommandUtils;
import me.st28.flexseries.flexcore.debug.DebugTest;
import me.st28.flexseries.flexcore.debug.DebugTestOutput;
import me.st28.flexseries.flexcore.item.CustomItemManager;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

public final class CustomItemDebugTest extends DebugTest<FlexCore> {

    public CustomItemDebugTest(FlexCore plugin) {
        super(plugin, "customitem", "Shows the underlying custom item ID on an item.");

        playerOnly = true;
    }

    @Override
    public DebugTestOutput runTest(CommandSender sender, String label, String[] args) {
        ItemStack handItem = CommandUtils.getSenderPlayer(sender).getItemInHand();

        if (handItem == null || handItem.getType() == Material.AIR) {
            return new DebugTestOutput(false, "Must be holding an item.");
        }

        String id = plugin.getModule(CustomItemManager.class).readId(handItem);
        if (id == null) {
            return new DebugTestOutput(false, "Held item is not a custom item.");
        }

        return new DebugTestOutput(true, "Found ID: " + id);
    }

}