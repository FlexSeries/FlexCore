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
package me.st28.flexseries.flexcore.message;

import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import me.st28.flexseries.flexcore.util.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Provides messages for a plugin.
 */
public class MessageProvider {

    private final static String UNKNOWN_MESSAGE = "&6<&cUnknown message: &4{PATH}&6>";

    JavaPlugin plugin;

    YamlFileManager file = null;
    private final Map<String, String> tags = new HashMap<>();
    private final Map<String, String> messages = new HashMap<>();

    MessageProvider(JavaPlugin plugin) {
        this.plugin = plugin;
        if (plugin == null) return;

        file = new YamlFileManager(plugin.getDataFolder() + File.separator + "messages.yml");
    }

    public void reload() throws IOException {
        file.reload();
        FileConfiguration config = file.getConfig();

        config.addDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("messages.yml"))));
        config.options().copyDefaults(true);
        file.save();
        file.reload();

        messages.clear();
        tags.clear();
        for (String category : config.getKeys(false)) {
            if (category.equalsIgnoreCase("tag")) {
                tags.put("default", config.getString(category));
                continue;
            }

            ConfigurationSection categorySec = config.getConfigurationSection(category);
            for (String key : categorySec.getKeys(false)) {
                loadKey(categorySec, key);
            }
        }

        if (!tags.containsKey("default")) {
            tags.put("default", "&8[&6{PLUGIN}&8] ");
        }
    }

    private void loadKey(ConfigurationSection section, String key) {
        Object obj = section.get(key);
        if (obj instanceof ConfigurationSection) {
            ConfigurationSection subSec = (ConfigurationSection) obj;
            for (String subKey : subSec.getKeys(false)) {
                loadKey(subSec, subKey);
            }
        } else {
            String objString = (String) obj;

            if (key.equalsIgnoreCase("tag")) {
                tags.put(section.getCurrentPath(), StringEscapeUtils.unescapeJava(objString));
            } else {
                messages.put(section.getCurrentPath() + "." + key, StringEscapeUtils.unescapeJava(objString));
            }
        }
    }

    private String getTag(String path) {
        String[] split = path.split("\\.");

        String tag = null;
        for (int i = split.length; i > 0; i--) {
            tag = tags.get(ArrayUtils.stringArrayToString(ArrayUtils.stringArraySublist(split, 0, i), "."));
            if (tag != null) break;
        }

        return tag == null ? tags.get("default") : tag;
    }

    final String getMessage(JavaPlugin plugin, String path) {
        String message = messages.get(path);
        return message == null
                ? UNKNOWN_MESSAGE.replace("{PATH}", path)
                : message.replace("{TAG}", getTag(path).replace("{PLUGIN}", plugin.getName()));
    }

    public String getMessage(String path) {
        return getMessage(this.plugin, path);
    }

    public String getMessage(String path, Map<String, String> replacements) {
        return getMessage(this.plugin, path, replacements);
    }

    final String getMessage(JavaPlugin plugin, String path, Map<String, String> replacements) {
        String message = messages.get(path);
        if (message != null) {
            for (Entry<String, String> entry : replacements.entrySet()) {
                message = message.replace(entry.getKey(), entry.getValue());
            }
        }
        return message == null
                ? UNKNOWN_MESSAGE.replace("{PATH}", path)
                : message.replace("{TAG}", getTag(path).replace("{PLUGIN}", plugin.getName()));
    }

}