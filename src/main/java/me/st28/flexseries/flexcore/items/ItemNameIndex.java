package me.st28.flexseries.flexcore.items;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import me.st28.flexseries.flexcore.utils.MaterialRef;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.Map.Entry;

public final class ItemNameIndex extends FlexModule<FlexCore> {

    private final Map<String, MaterialRef> nameIndex = new HashMap<>();

    public ItemNameIndex(FlexCore plugin) {
        super(plugin, "item_names", "Allows for easier item lookup based on name");
    }

    @Override
    protected void handleReload() {
        nameIndex.clear();
        FileConfiguration config = getConfig();

        ConfigurationSection itemSec = config.getConfigurationSection("Items");
        if (itemSec == null) {
            return;
        }

        for (String rawMat : itemSec.getKeys(false)) {
            String materialName;
            Material material;
            short damage;

            String[] split = rawMat.split(":");
            if (split.length == 2) {
                try {
                    damage = Short.valueOf(split[1]);
                } catch (NumberFormatException ex) {
                    LogHelper.warning(this, "Invalid damage value '" + split[1] + "' in raw material '" + rawMat + "'");
                    continue;
                }
            } else {
                damage = 0;
            }

            materialName = split[0];

            try {
                material = Material.valueOf(materialName.toUpperCase());
            } catch (Exception ex) {
                LogHelper.warning(this, "Invalid material '" + rawMat + "'");
                continue;
            }

            MaterialRef materialRef = new MaterialRef(material, damage);
            for (String alias : itemSec.getStringList(rawMat)) {
                if (nameIndex.containsKey(alias.toLowerCase())) {
                    LogHelper.warning(this, "Material alias '" + alias + "' is already in use.");
                    continue;
                }

                nameIndex.put(alias.toLowerCase(), materialRef);
            }
        }
    }

    /**
     * @return a material that matches the input name.
     */
    public MaterialRef getMaterial(String name) {
        Validate.notNull(name, "Name cannot be null.");

        name = name.toLowerCase();
        List<String> found = new ArrayList<>();
        for (Entry<String, MaterialRef> entry : nameIndex.entrySet()) {
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
    public Collection<String> getAliases(MaterialRef material) {
        Validate.notNull(material, "Material cannot be null.");

        List<String> aliases = new ArrayList<>();
        for (Entry<String, MaterialRef> entry : nameIndex.entrySet()) {
            if (entry.getValue().equals(material)) {
                aliases.add(entry.getKey());
            }
        }

        return aliases;
    }

}