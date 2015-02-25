package me.st28.flexseries.flexcore.items;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.Map.Entry;

public final class ItemNameIndex extends FlexModule<FlexCore> {

    private final Map<String, Material> nameIndex = new HashMap<>();

    public ItemNameIndex(FlexCore plugin) {
        super(plugin, "item_names", "Allows for easier item lookup based on name");
    }

    @Override
    protected void handleReload() {
        FileConfiguration config = getConfig();

        ConfigurationSection itemSec = config.getConfigurationSection("items");
        if (itemSec == null) {
            return;
        }

        for (String rawMat : itemSec.getKeys(false)) {
            Material material;
            try {
                material = Material.valueOf(rawMat.toUpperCase());
            } catch (Exception ex) {
                LogHelper.warning(this, "Invalid material '" + rawMat + "'");
                continue;
            }

            for (String alias : itemSec.getStringList(rawMat)) {
                if (nameIndex.containsKey(alias.toLowerCase())) {
                    LogHelper.warning(this, "Material alias '" + alias + "' is already in use.");
                    continue;
                }

                nameIndex.put(alias.toLowerCase(), material);
            }
        }
    }

    /**
     * @return a material that matches the input name.
     */
    public Material getMaterial(String name) {
        Validate.notNull(name, "Name cannot be null.");

        name = name.toLowerCase();
        List<String> found = new ArrayList<>();
        for (Entry<String, Material> entry : nameIndex.entrySet()) {
            String key = entry.getKey();

            if (name.equals(key)) {
                return entry.getValue();
            }

            if (key.contains(name)) {
                found.add(key);
            }
        }

        if (found.size() != 0) {
            return null;
        }

        return nameIndex.get(found.get(0));
    }

    /**
     * @return a collection of available aliases for a given material.
     */
    public Collection<String> getAliases(Material material) {
        Validate.notNull(material, "Material cannot be null.");

        List<String> aliases = new ArrayList<>();
        for (Entry<String, Material> entry : nameIndex.entrySet()) {
            if (entry.getValue() == material) {
                aliases.add(entry.getKey());
            }
        }

        return aliases;
    }

}