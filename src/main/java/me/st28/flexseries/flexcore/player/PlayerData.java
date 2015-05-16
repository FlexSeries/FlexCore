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

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;

public final class PlayerData {

    private final UUID uuid;

    Long firstJoin;
    Long lastLogin;
    Long lastLogout;

    String lastIp;
    final List<String> ips = new ArrayList<>();

    String lastName;
    final List<String> names = new ArrayList<>();

    final Map<String, Object> customData = new HashMap<>();

    PlayerData(UUID uuid, FileConfiguration config) {
        this.uuid = uuid;

        if (config.isSet("firstJoin")) {
            firstJoin = config.getLong("firstJoin");
        }

        if (config.isSet("lastlogin")) {
            lastLogin = config.getLong("lastlogin");
        }

        if (config.isSet("lastLogout")) {
            lastLogout = config.getLong("lastLogout");
        }

        lastIp = config.getString("ip.last");
        ips.addAll(config.getStringList("ip.previous"));

        lastName = config.getString("name.last");
        names.addAll(config.getStringList("name.previous"));

        ConfigurationSection customSec = config.getConfigurationSection("custom");
        if (customSec != null) {
            loadCustomData(customSec, "");
        }
    }

    private void loadCustomData(ConfigurationSection currentSec, String currentKey) {
        for (String key : currentSec.getKeys(false)) {
            if (currentSec.get(key) instanceof ConfigurationSection) {
                loadCustomData(currentSec.getConfigurationSection(key), currentKey + "." + key);
            } else {
                customData.put(currentKey + "." + key, currentSec.get(key));
            }
        }
    }

    void save(ConfigurationSection config) {
        config.set("firstJoin", firstJoin);
        config.set("lastLogin", lastLogin);
        config.set("lastLogout", lastLogout);
        config.set("ip.last", lastIp);
        config.set("ip.previous", ips);
        config.set("name.last", lastName);
        config.set("name.previous", names);

        ConfigurationSection customSec = config.createSection("custom");
        for (Entry<String, Object> entry : customData.entrySet()) {
            customSec.set(entry.getKey(), entry.getValue());
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public Timestamp getFirstJoin() {
        return firstJoin == null ? null : new Timestamp(firstJoin);
    }

    public Timestamp getLastLogin() {
        return lastLogin == null ? null : new Timestamp(lastLogin);
    }

    public Timestamp getLastLogout() {
        return lastLogout == null ? null : new Timestamp(lastLogout);
    }

    public String getLastIp() {
        return lastIp;
    }

    public List<String> getIps() {
        return Collections.unmodifiableList(ips);
    }

    public String getLastName() {
        return lastName;
    }

    public List<String> getNames() {
        return Collections.unmodifiableList(names);
    }

    public <T> T getCustomData(String key, T type) {
        Validate.notNull(key, "Key cannot be null.");
        Validate.notNull(type, "Type cannot be null.");
        return (T) customData.get(key);
    }

    public void setCustomData(String key, Object data) {
        Validate.notNull(key, "Key cannot be null.");

        if (data == null) {
            customData.remove(key);
        } else {
            customData.put(key, data);
        }
    }

}