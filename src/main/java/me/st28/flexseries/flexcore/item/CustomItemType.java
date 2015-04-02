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
package me.st28.flexseries.flexcore.item;

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