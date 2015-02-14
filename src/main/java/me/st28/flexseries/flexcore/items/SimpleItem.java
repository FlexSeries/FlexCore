package me.st28.flexseries.flexcore.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleItem {

    private final static Pattern SPACE_PATTERN = Pattern.compile("(?<!\\\\)_");
    private final static Pattern LIST_SPLIT_PATTERN = Pattern.compile("(?<!\\\\)(,)");
    private final static Pattern FLAG_PATTERN = Pattern.compile("\\s+-(amt|name|lore|enchants)=(\\S+)", Pattern.CASE_INSENSITIVE);

    private Material material;
    private short damage;
    private int amount = 1;

    private ItemMeta meta;
    /*private String displayName;
    private List<String> lore = new ArrayList<>();*/
    private Map<Enchantment, Integer> enchantments = new HashMap<>();

    /**
     * Creates an item from a material.
     *
     * @param material Material type for the item.
     */
    public SimpleItem(Material material) {
        this.material = material;
    }

    /**
     * Creates an item from a material and specified damage amount.
     *
     * @param material Material type for the item.
     * @param damage Damage value for the item.
     */
    public SimpleItem(Material material, short damage) {
        this.material = material;
        damage(damage);
    }

    /**
     * Creates an item from an existing Bukkit ItemStack.
     *
     * @param itemStack ItemStack to create this SimpleItem from.
     */
    public SimpleItem(ItemStack itemStack) {
        material = itemStack.getType();
        amount = itemStack.getAmount();
        damage = itemStack.getDurability();

        if (itemStack.hasItemMeta()) {
            this.meta = itemStack.getItemMeta();
        }
    }

    /**
     * Creates an item from an existing SimpleItem instance.
     *
     * @param simpleItem The other SimpleItem instance to use.
     */
    public SimpleItem(SimpleItem simpleItem) {
        this.material = simpleItem.material;
        this.damage = simpleItem.damage;
        this.amount = simpleItem.amount;

        this.meta = simpleItem.meta.clone();
    }

    /**
     * Creates an item from a string.<br />
     * Format: ITEM_NAME:DAMAGE -amt=INTEGER -name=STRING -lore=STRING,STRING -enchants=NAME:LEVEL,NAME:LEVEL<br />
     * Spaces in the name and lore sections can be done by putting an underscore (_).  If you want to include an underscore, escape it with \ (\_)<br />
     * You can escape commas in the lore by putting a \ as well, (\,).
     *
     * @param input The string to use.
     * @throws RuntimeException Various exceptions that can occur:
     *         <ul>
     *             <li>Illegal material name</li>
     *             <li>Damage isn't a short</li>
     *             <li>Amount isn't an integer</li>
     *         </ul>
     */
    public SimpleItem(String input) {
        String firstPart = input.split(" ")[0];

        String[] firstSplit = firstPart.split(":");
        material = Material.valueOf(firstSplit[0].toUpperCase());
        if (firstSplit.length > 1) {
            damage = Short.parseShort(firstSplit[1]);
        } else {
            damage = 0;
        }
        meta = Bukkit.getItemFactory().getItemMeta(material);

        Matcher matcher = FLAG_PATTERN.matcher(input);
        while (matcher.find()) {
            switch (matcher.group(1).toLowerCase()) {
                case "amt":
                    amount = Integer.parseInt(matcher.group(2));
                    break;

                case "name":
                    meta.setDisplayName(polishString(matcher.group(2)));
                    break;

                case "lore":
                    readLore(LIST_SPLIT_PATTERN.split(matcher.group(2)));
                    break;

                case "enchants":
                    readEnchants(LIST_SPLIT_PATTERN.split(matcher.group(2)));
                    break;
            }
        }
    }

    private String polishString(String input) {
        return SPACE_PATTERN.matcher(input).replaceAll(" ").replace("\\_", "_").replace("\\,", ",");
    }

    private void readLore(String[] rawInput) {
        List<String> lore = new ArrayList<>();
        for (String string : rawInput) {
            lore.add(polishString(string));
        }
        meta.setLore(lore);
    }

    private void readEnchants(String[] rawInput) {
        for (String string : rawInput) {
            String[] split = string.split(":");
            Enchantment enchant = Enchantment.getByName(split[0].toUpperCase());
            int level;
            if (split.length > 1) {
                level = Integer.parseInt(split[1]);
            } else {
                level = 1;
            }
            enchantments.put(enchant, level);
        }
    }

    /**
     * Returns the Material of the item.
     *
     * @return The Material set for the item.
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Sets the material of the item.
     *
     * @param material The material of the item.
     * @return The SimpleItem instance, for chaining.
     */
    public SimpleItem material(Material material) {
        this.material = material;
        return this;
    }

    /**
     * Returns the damage (durability) set for the item.
     *
     * @return Damage value of the item.
     */
    public short getDamage() {
        return damage;
    }

    /**
     * Sets the damage (durability) of the item.
     *
     * @param damage The damage to set.<br />
     *               If < 0, will automatically be set to 0.
     * @return The SimpleItem instance, for chaining.
     */
    public SimpleItem damage(short damage) {
        if (damage < 0) {
            this.damage = 0;
        } else {
            this.damage = damage;
        }
        return this;
    }

    /**
     * Returns the amount set for the item.
     *
     * @return The amount of the resulting ItemStack, when created.
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Sets the amount of the item.
     *
     * @param amount The amount to set.<br />
     *               If <= 0, will automatically be set to 1.
     * @return The SimpleItem instance, for chaining.
     */
    public SimpleItem amount(int amount) {
        if (amount <= 0) {
            this.amount = 1;
        } else {
            this.amount = amount;
        }
        return this;
    }

    /**
     * Returns the display name that is set for the item.
     *
     * @return The display name set for the item.<br />
     *         Null if there is no display name set.
     */
    public String getDisplayName() {
        return meta.getDisplayName();
    }

    /**
     * Sets the display name of the item.
     *
     * @param displayName The display name to set.<br />
     *                    Null to remove the display name.
     * @return The SimpleItem instance, for chaining.
     */
    public SimpleItem displayName(String displayName) {
        meta.setDisplayName(displayName);
        return this;
    }

    /**
     * Returns the lore of the item.
     *
     * @return An unmodifiable view of the lore of the item.<br />
     *         Null if no lore is set for the item.
     */
    public List<String> getLore() {
        List<String> lore = meta.hasLore() ? meta.getLore() : null;
        return lore == null ? null : Collections.unmodifiableList(lore);
    }

    /**
     * Sets the lore of the item.
     *
     * @param lore The lore to set.<br />
     *             Null to remove the lore.
     * @return The SimpleItem instance, for chaining.
     */
    public SimpleItem lore(List<String> lore) {
        meta.setLore(lore);
        return this;
    }

    /**
     * Returns all of the enchantments that are set for the item.
     *
     * @return A read-only view of the item's enchantments.<br />
     *         Null if there are no enchantments.
     */
    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments.isEmpty() ? null : Collections.unmodifiableMap(enchantments);
    }

    /**
     * Sets the enchantments on the item.
     *
     * @param enchants The enchants to set.<br />
     *                 Null or an empty map to remove the enchants.
     * @return The SimpleItem instance, for chaining.
     */
    public SimpleItem enchants(Map<Enchantment, Integer> enchants) {
        this.enchantments.clear();
        if (enchants != null && !enchants.isEmpty()) {
            enchantments.putAll(enchants);
        }

        return this;
    }

    /**
     * Builds the item.
     *
     * @return Newly created item.
     */
    public ItemStack getItem() {
        if (material == null) {
            throw new IllegalStateException("No material is set.");
        }

        ItemMeta newMeta = meta.clone();

        ItemStack item = new ItemStack(material, amount, damage);

        if (!enchantments.isEmpty()) {
            for (Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                newMeta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
        }

        item.setItemMeta(newMeta);
        return item;
    }

}