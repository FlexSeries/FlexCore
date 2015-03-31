package me.st28.flexseries.flexcore.util;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utility methods related to the {@link java.util.UUID} class.
 */
public final class UuidUtils {

    private static final Pattern UUID_FIXER_PATTERN = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})");

    private UuidUtils() {}

    public static UUID fromStringNoDashes(String rawUuid) {
        return UUID.fromString(UUID_FIXER_PATTERN.matcher(rawUuid).replaceAll("$1-$2-$3-$4-$5"));
    }

}