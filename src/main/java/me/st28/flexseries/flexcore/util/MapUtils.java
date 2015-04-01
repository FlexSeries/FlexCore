package me.st28.flexseries.flexcore.util;

import org.apache.commons.lang.Validate;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility methods for Java's Map class.
 */
public final class MapUtils {

    private MapUtils() {}

    /**
     * Retrieves an entry from a map with a given index. Intended for maps that preserve ordering (ex. LinkedHashMap)
     *
     * @param map The map to retrieve the entry from.
     * @param index The index of the entry in the map.
     * @return The entry at the given index in the given map.
     */
    public static <K, V> Entry<K, V> getEntryByIndex(Map<K, V> map, int index) {
        Validate.notNull(map, "Map cannot be null.");
        int mapSize = map.size();

        int curIndex = 0;
        Iterator<Entry<K, V>> iterator = map.entrySet().iterator();
        while (iterator.hasNext() && curIndex < mapSize) {
            if (curIndex++ == index) {
                return iterator.next();
            }
        }
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + mapSize);
    }

}