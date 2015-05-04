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
package me.st28.flexseries.flexcore.util;

import me.st28.flexseries.flexcore.player.PlayerProfile;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.UUID;

/**
 * Utility classes for Bukkit's Player class.
 */
public final class PlayerUtils {

    private PlayerUtils() {}

    /**
     * @return the name for the given UUID.
     */
    public static String getName(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null.");
        return new PlayerProfile(uuid).getName();
    }

    /**
     * @return the name for the given UUID in green if the player is offline or red if the player is offline.
     */
    public static String getColoredName(UUID uuid) {
        Validate.notNull(uuid, "UUID cannot be null.");
        String name = new PlayerProfile(uuid).getName();
        return (Bukkit.getPlayer(uuid) != null ? ChatColor.GREEN : ChatColor.RED) + name;
    }

}