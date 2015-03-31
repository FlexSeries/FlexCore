package me.st28.flexseries.flexcore.util;

import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a material.
 */
public final class MaterialRef {

    private final Material material;
    private final short damage;

    public MaterialRef(Material material, short damage) {
        Validate.notNull(material, "Material cannot be null.");
        Validate.isTrue(damage >= 0, "Damage value must be 0 or more.");

        this.material = material;
        this.damage = damage;
    }

    public MaterialRef(ItemStack item) {
        Validate.notNull(item, "Item cannot be null.");

        this.material = item.getType();
        this.damage = item.getDurability();
    }

    public final Material getMaterial() {
        return material;
    }

    public final short getDamage() {
        return damage;
    }

    @Override
    public String toString() {
        return material.toString() + ":" + damage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MaterialRef that = (MaterialRef) o;

        return damage == that.damage && material == that.material;
    }

    @Override
    public int hashCode() {
        int result = material.hashCode();
        result = 31 * result + (int) damage;
        return result;
    }

}