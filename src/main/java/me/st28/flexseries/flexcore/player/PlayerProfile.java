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
package me.st28.flexseries.flexcore.player;

import me.st28.flexseries.flexcore.player.uuid_tracker.PlayerUuidTracker;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import org.bukkit.ChatColor;

import java.util.UUID;

/**
 * A utility class for having a representation of a player and for easily retrieving the name and display name of a player.
 */
public final class PlayerProfile implements Comparable<PlayerProfile> {

    private final UUID uuid;
    private final String name;

    public PlayerProfile(UUID uuid) {
        this.uuid = uuid;

        PlayerUuidTracker uuidTracker = FlexPlugin.getRegisteredModule(PlayerUuidTracker.class);
        name = uuidTracker.getName(uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getTopLevelName() {
        return name == null ? uuid.toString() : name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerProfile profile = (PlayerProfile) o;

        if (name != null ? !name.equals(profile.name) : profile.name != null) return false;
        if (!uuid.equals(profile.uuid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(PlayerProfile o) {
        if (this.name == null && o.name != null) {
            return -1;
        } else if (this.name != null && o.name == null) {
            return 1;
        }

        String name = ChatColor.stripColor(getTopLevelName());
        String oName = ChatColor.stripColor(o.getTopLevelName());

        return name.compareTo(oName);
    }

}