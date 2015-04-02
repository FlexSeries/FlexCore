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

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.item.events.*;
import me.st28.flexseries.flexcore.plugin.module.FlexModule;
import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tracks and handles custom items.<br />
 * Custom items are tracked via a unique identifier in the display name of the item.
 *
 * <h1>Terms</h1>
 * <ul>
 *     <li><b>IID</b> - Item ID.  The unique identifier for a custom item.</li>
 * </ul>
 */
public final class CustomItemManager extends FlexModule<FlexCore> implements Listener {

    /**
     * Valid characters that can be used for an IID. (color codes)
     */
    private final static char[] IID_CHARACTERS = "abcdef0123456789".toCharArray();

    /**
     * The pattern to capture an IID from the display name of a custom item.
     */
    private final static Pattern IID_PATTERN = Pattern.compile("^((?:&[a-f0-9]){5,10})&r.+");

    /**
     * The pattern to capture an IID from the save file for a custom item.
     */
    private final static Pattern IID_FILE_PATTERN = Pattern.compile("^([a-f0-9]{10}).yml");

    /**
     * The length to use for an IID.
     */
    private final static int IID_LENGTH = 10;

    /**
     * Registered custom item types.<br />
     * <b>Structure:</b> <code>IID, CustomItemType</code>
     */
    private final Map<String, CustomItemType> itemTypes = new HashMap<>();

    /**
     * Stores the IIDs that are used for singleton CustomItemTypes.
     */
    private YamlFileManager singletonTypeIdFile;

    /**
     * <b>Structure:</b> <code>type identifier, IID</code>
     */
    private final Map<String, String> singletonTypeIIDs = new HashMap<>();

    /**
     * The storage location for custom item data.
     */
    private File customItemDir;

    /**
     * Custom items that are awaiting the corresponding {@link CustomItemType} to be registered.
     */
    private final Map<String, ConfigurationSection> pendingActiveItems = new HashMap<>();

    /**
     * Loaded custom items.<br />
     * <b>Structure:</b> <code>IID, CustomItem</code>
     */
    private final Map<String, CustomItem> activeItems = new HashMap<>();

    public CustomItemManager(FlexCore plugin) {
        super(plugin, "custom_items", "Tracks and handles custom items", true);
    }

    @Override
    public void handleLoad() {
        customItemDir = new File(getDataFolder() + File.separator + "items");
        customItemDir.mkdir();

        // Load the singleton IIDs.
        singletonTypeIdFile = new YamlFileManager(getDataFolder() + File.separator + "singletonTypeIIDs.yml");
        FileConfiguration singletonConfig = singletonTypeIdFile.getConfig();
        for (String identifier : singletonConfig.getKeys(false)) {
            singletonTypeIIDs.put(identifier, singletonConfig.getString(identifier));
        }

        // Load the custom items.
        for (File file : customItemDir.listFiles()) {
            final Matcher matcher = IID_FILE_PATTERN.matcher(file.getName());
            if (matcher.matches()) {
                pendingActiveItems.put(matcher.group(1), new YamlFileManager(file).getConfig());
            }
        }
    }

    @Override
    protected void handleReload() {
        customItemDir.mkdir();
    }

    @Override
    public void handleSave(boolean async) {
        // Save the singleton item IIDs.
        FileConfiguration singletonConfig = singletonTypeIdFile.getConfig();
        for (Entry<String, String> entry : singletonTypeIIDs.entrySet()) {
            singletonConfig.set(entry.getKey(), entry.getValue());
        }
        singletonTypeIdFile.save();

        // Save custom item data.
        for (Entry<String, CustomItem> entry : activeItems.entrySet()) {
            final CustomItem entryItem = entry.getValue();
            final YamlFileManager entryFile = new YamlFileManager(customItemDir + File.separator + entry.getKey() + ".yml");
            final FileConfiguration entryConfig = entryFile.getConfig();

            entryConfig.set("type", entryItem.type.getIdentifier());

            ConfigurationSection dataSection = entryConfig.getConfigurationSection("data");
            if (dataSection == null) {
                dataSection = entryConfig.createSection("data");
            }

            for (Entry<String, Object> dataEntry : entryItem.persistentCustomData.entrySet()) {
                dataSection.set(dataEntry.getKey(), dataEntry.getValue());
            }

            entryFile.save();
        }
    }

    /**
     * Registers a {@link CustomItemType} with the manager.
     *
     * @return True if successfully registered.<br />
     *         False if an item type with the same identifier is already registered.
     */
    public boolean registerItemType(CustomItemType type) {
        Validate.notNull(type, "Type cannot be null.");

        String identifier = type.getIdentifier();
        if (itemTypes.containsKey(identifier)) {
            return false;
        }

        if (type.isSingleton && !singletonTypeIIDs.containsKey(type.getIdentifier())) {
            singletonTypeIIDs.put(type.getIdentifier(), generateId());
        }

        itemTypes.put(identifier, type);
        loadPendingItems(type);
        return true;
    }

    private void loadPendingItems(CustomItemType type) {
        final String identifier = type.getIdentifier();

        final List<String> loadedIIDs = new ArrayList<>();
        for (Entry<String, ConfigurationSection> entry : pendingActiveItems.entrySet()) {
            final ConfigurationSection config = entry.getValue();
            final String curIID = entry.getKey();
            final String curIdentifier = config.getString("type", null);

            if (curIdentifier.equals(identifier)) {
                CustomItem customItem = new CustomItem(curIID, type);

                final ConfigurationSection dataSec = config.getConfigurationSection("data");
                if (dataSec != null) {
                    customItem.persistentCustomData.putAll(dataSec.getValues(false));
                }

                activeItems.put(curIdentifier, customItem);
                loadedIIDs.add(curIID);
            }
        }

        for (String id : loadedIIDs) {
            pendingActiveItems.remove(id);
        }
    }

    /**
     * @return a newly generated ID for a custom item.
     */
    private String generateId() {
        int length = IID_LENGTH;
        Random random = new Random();
        String id = null;
        while (id == null || activeItems.containsKey(id) || pendingActiveItems.containsKey(id)) {
            char[] text = new char[length];
            for (int i = 0; i < length; i++) {
                text[i] = IID_CHARACTERS[random.nextInt(IID_CHARACTERS.length)];
            }
            id = String.valueOf(text);
        }
        return id;
    }

    /**
     * Reads the ID stored on an item.
     *
     * @param item The item to examine.
     * @return The ID stored on the item.<br />
     *         Null if the item is not a custom item.
     */
    public String readId(ItemStack item) {
        Validate.notNull(item, "Item cannot be null.");
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return null;

        String id = null;

        String name = item.getItemMeta().getDisplayName().replace(String.valueOf(ChatColor.COLOR_CHAR), "&");
        Matcher matcher = IID_PATTERN.matcher(name);
        if (matcher.matches()) {
            id = matcher.group();
        }
        return id == null ? null : id.replace("&", "");
    }

    /**
     * @param item The item to get the custom instance of.
     * @return the custom item instance for an item.<br />
     *         Null if the item does not represent a custom item.
     */
    public CustomItem getCustomItem(ItemStack item) {
        Validate.notNull(item, "Item cannot be null.");

        String id = readId(item);
        return id == null ? null : activeItems.get(id);
    }

    /**
     * Creates a new instance of an item type.
     *
     * @param type The item type to create.
     * @param persistentData Custom data for the item that should be saved and loaded to the disk.
     * @param temporaryData Custom data for the item that is only temporary.
     * @return The newly created custom item.
     */
    public CustomItem createCustomItem(CustomItemType type, Map<String, Object> persistentData, Map<String, Object> temporaryData) {
        Validate.notNull(type, "Type cannot be null.");

        if (type.isSingleton) {
            return activeItems.get(singletonTypeIIDs.get(type.getIdentifier()));
        }

        final String id = generateId();
        final CustomItem item = new CustomItem(id, type);

        if (persistentData != null) {
            item.persistentCustomData.putAll(persistentData);
        }

        if (temporaryData != null) {
            item.temporaryCustomData.putAll(temporaryData);
        }

        activeItems.put(id, item);
        return item;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent e) {
        if (e.isCancelled()) return;

        final Player p = e.getPlayer();

        final ItemStack prevItem = p.getInventory().getItem(e.getPreviousSlot());

        if (prevItem != null) {
            CustomItem prevCustomItem = getCustomItem(prevItem);
            if (prevCustomItem != null) {
                CustomItemUnholdEvent newEvent = new CustomItemUnholdEvent(p, prevCustomItem);
                Bukkit.getPluginManager().callEvent(newEvent);

                if (newEvent.isCancelled()) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        final ItemStack newItem = p.getInventory().getItem(e.getNewSlot());

        if (newItem != null) {
            CustomItem newCustomItem = getCustomItem(newItem);
            if (newCustomItem != null) {
                CustomItemHoldEvent newEvent = new CustomItemHoldEvent(p, newCustomItem);
                Bukkit.getPluginManager().callEvent(newEvent);

                if (newEvent.isCancelled()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDespawn(final ItemDespawnEvent e) {
        if (e.isCancelled()) return;

        CustomItem customItem = getCustomItem(e.getEntity().getItemStack());
        if (customItem != null) {
            final CustomItemDestroyEvent destroyEvent = new CustomItemDestroyEvent(customItem);
            Bukkit.getPluginManager().callEvent(destroyEvent);

            if (destroyEvent.isCancelled()) {
                e.setCancelled(true);
                return;
            }

            final CustomItemDespawnEvent despawnEvent = new CustomItemDespawnEvent(customItem);
            Bukkit.getPluginManager().callEvent(despawnEvent);

            if (despawnEvent.isCancelled()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityCombust(final EntityCombustEvent e) {
        if (e.isCancelled() || !(e.getEntity() instanceof Item)) return;

        CustomItem customItem = getCustomItem(((Item) e.getEntity()).getItemStack());
        if (customItem != null) {
            final CustomItemDestroyEvent destroyEvent = new CustomItemDestroyEvent(customItem);
            Bukkit.getPluginManager().callEvent(destroyEvent);

            if (destroyEvent.isCancelled()) {
                e.setCancelled(true);
                return;
            }

            final CustomItemCombustEvent combustEvent = new CustomItemCombustEvent(customItem);
            Bukkit.getPluginManager().callEvent(combustEvent);

            if (combustEvent.isCancelled()) {
                e.setCancelled(true);
            }
        }
    }

}