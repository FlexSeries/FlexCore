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