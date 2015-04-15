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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Various utility methods for Strings.
 */
public final class StringUtils {

    private StringUtils() { }

    public static <T> String collectionToString(Collection<T> collection, StringConverter<T> converter) {
        return collectionToString(collection, converter, " ");
    }

    public static <T> String collectionToString(Collection<T> collection, StringConverter<T> converter, String delimiter) {
        return collectionToString(collection, converter, delimiter, null);
    }

    public static <T> String collectionToString(Collection<T> collection, StringConverter<T> converter, String delimiter, String defaultValue) {
        if (collection == null) return "";

        StringBuilder sb = new StringBuilder();
        for (T item : collection) {
            if (sb.length() > 0) {
                sb.append(delimiter);
            }
            sb.append(converter.toString(item));
        }
        return sb.length() == 0 ? defaultValue : sb.toString();
    }

    public static <T> List<String> collectionToStringList(Collection<T> collection, StringConverter<T> converter) {
        List<String> returnList = new ArrayList<>();
        for (T item : collection) {
            returnList.add(converter.toString(item));
        }
        return returnList;
    }

    public static <T, F extends Collection<String>>  F collectionToStringCollection(Collection<T> collection, StringConverter<T> converter, F returnCollection) {
        for (T item : collection) {
            returnCollection.add(converter.toString(item));
        }
        return returnCollection;
    }

    public static String stringCollectionToString(Collection<String> collection) {
        return stringCollectionToString(collection, " ");
    }

    /**
     * Converts a string collection into a clean string.
     *
     * @param collection The collection to convert.
     * @param delimiter What to place between the strings from the collection.
     * @return A string containing the contents of the collection, separated by the given delimiter.
     */
    public static String stringCollectionToString(Collection<String> collection, String delimiter) {
        if (collection == null) return "";
        StringBuilder sb = new StringBuilder();
        for (String item : collection) {
            if (sb.length() > 0) {
                sb.append(delimiter);
            }
            sb.append(item);
        }
        return sb.toString();
    }

    /**
     * * @see #stringCollectionToString(java.util.Collection)
     */
    @Deprecated
    public static String stringListToString(List<String> list) {
        return StringUtils.stringCollectionToString(list, " ");
    }

    /**
     * @see #stringCollectionToString(java.util.Collection, String)
     */
    @Deprecated
    public static String stringListToString(List<String> list, String delimiter) {
        return StringUtils.stringCollectionToString(list, delimiter);
    }

    public static String[] splitStringByLength(String input, int length) {
        List<String> output = new ArrayList<>();

        while (input.length() > length) {
            output.add(input.substring(0, length));
            input = input.substring(length);
        }

        if (input.length() > 0) {
            output.add(input);
        }

        return output.toArray(new String[output.size()]);
    }

    public static boolean equalsIgnoreCaseMultiple(String input, String... other) {
        for (String str: other) {
            if (input.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

}