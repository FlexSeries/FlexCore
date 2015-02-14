package me.st28.flexseries.flexcore.utils;

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
        if (collection == null) return "";
        StringBuilder sb = new StringBuilder();
        for (T item : collection) {
            if (sb.length() > 0) {
                sb.append(delimiter);
            }
            sb.append(converter.toString(item));
        }
        return sb.toString();
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