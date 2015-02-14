package me.st28.flexseries.flexcore.items;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CustomItemManager extends FlexModule<FlexCore> {

    /**
     * Valid characters that can be used in the name of an item (color codes).
     */
    private final static char[] VALID_CHARACTERS = "abcdef0123456789".toCharArray();

    /**
     * The pattern to capture an item ID from the display name of an item.
     */
    private final static Pattern IID_PATTERN = Pattern.compile("^((?:&[a-f0-9]){5,10})&r.+");

    /**
     * The length of an <b>I</b>tem <b>ID</b>.
     */
    private final static int IID_LENGTH = 10;

    /**
     * Registered custom item types.
     */
    private final Map<String, CustomItemType> itemTypes = new HashMap<>();

    /**
     * Stores the single IDs used for CustomItemTypes with single instances.
     */
    private YamlFileManager singletonTypeIdFile;
    private final Map<String, String> singletonTypeIds = new HashMap<>();

    private YamlFileManager activeItemFile;
    private final Map<String, CustomItem> activeItems = new HashMap<>();
    private final Map<String, String> pendingActiveItems = new HashMap<>();

    public CustomItemManager(FlexCore plugin) {
        super(plugin, "custom_items", "Tracks and handles custom items");
    }

    @Override
    public void handleLoad() throws Exception {
        File customItemDir = new File(plugin.getDataFolder() + File.separator + "custom_items");
        customItemDir.mkdir();

        singletonTypeIdFile = new YamlFileManager(customItemDir + File.separator + "singletonTypeIds.yml");
        FileConfiguration singletonConfig = singletonTypeIdFile.getConfig();
        for (String identifier : singletonConfig.getKeys(false)) {
            singletonTypeIds.put(identifier, singletonConfig.getString(identifier));
        }

        activeItemFile = new YamlFileManager(customItemDir + File.separator + "activeItems.yml");
        FileConfiguration config = activeItemFile.getConfig();
        for (String id : config.getKeys(false)) {
            pendingActiveItems.put(id, config.getString(id));
        }
    }

    @Override
    public void handleSave(boolean async) {
        FileConfiguration config = activeItemFile.getConfig();
        for (Entry<String, CustomItem> entry : activeItems.entrySet()) {
            config.set(entry.getKey(), entry.getValue().type.getIdentifier());
        }

        for (Entry<String, String> entry : pendingActiveItems.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }

        activeItemFile.save();
    }

    /**
     * Registers a custom item type with the manager.
     *
     * @param type The type to register.
     * @return True if successfully registered.<br />
     *         False if an item type with the same identifier is already registered.
     */
    public boolean registerItemType(CustomItemType type) {
        String identifier = type.getIdentifier();
        if (itemTypes.containsKey(identifier)) {
            return false;
        }

        if (!type.allowsMultipleInstances && !singletonTypeIds.containsKey(type.getIdentifier())) {
            singletonTypeIds.put(type.getIdentifier(), generateId());
        }

        itemTypes.put(identifier, type);
        loadPendingItems(type);
        return true;
    }

    private void loadPendingItems(CustomItemType type) {
        String identifier = type.getIdentifier();

        List<String> loadedIds = new ArrayList<>();
        for (Entry<String, String> entry : pendingActiveItems.entrySet()) {
            String curIdentifier = entry.getValue();

            if (curIdentifier.equals(identifier)) {
                activeItems.put(entry.getKey(), new CustomItem(entry.getKey(), type));
                loadedIds.add(entry.getKey());
            }
        }

        for (String id : loadedIds) {
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
                text[i] = VALID_CHARACTERS[random.nextInt(VALID_CHARACTERS.length)];
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
        String id = readId(item);
        return id == null ? null : activeItems.get(id);
    }

    /**
     * Creates a new instance of an item type.
     *
     * @param type The item type to create.
     * @param extraData Extra data to include in the item.
     * @return The newly created custom item.
     */
    public CustomItem createCustomItem(CustomItemType type, Map<String, Object> extraData) {
        String id = !type.allowsMultipleInstances ? singletonTypeIds.get(type.getIdentifier()) : generateId();
        CustomItem item = new CustomItem(id, type);
        if (extraData != null && !extraData.isEmpty()) {
            item.customData.putAll(extraData);
        }

        if (!activeItems.containsKey(id)) {
            activeItems.put(id, item);
        }
        return item;
    }

}