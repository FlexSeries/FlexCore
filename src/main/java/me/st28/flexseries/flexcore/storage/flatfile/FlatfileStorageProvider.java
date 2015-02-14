package me.st28.flexseries.flexcore.storage.flatfile;

import java.io.File;

/**
 * An abstraction layer for all flatfile storage providers.<br />
 * <b>INCOMPLETE && UNUSED AT THE MOMENT.</b>
 */
public abstract class FlatfileStorageProvider {

    /**
     * @return the underlying file for this provider.
     */
    public abstract File getFile();

    /**
     * Reloads the file.
     */
    public void reload() { }

    /**
     * Sets a value.
     * @param key The key for the value.
     * @param value The value to set for the given key.
     */
    public abstract void setValue(String key, Object value);

    /**
     * @return true if there is a value set for the given key.
     */
    public abstract boolean isSet(String key);

    /**
     * @return a string value set for a given key.
     */
    public abstract String getString(String key);

    /**
     * @return a string value set for a given key.<br />
     *         If null, will return the default value provided.
     */
    public String getString(String key, String def) {
        String returnValue = getString(key);
        return returnValue == null ? def : returnValue;
    }

    /**
     * @return an integer value set for a given key.
     */
    public abstract int getInt(String key);

    /**
     * @return an integer value set for a given key.<br />
     *         If not set, will return the default value provided.
     */
    public int getInt(String key, int def) {
        return !isSet(key) ? def : getInt(key);
    }

    /**
     * @return a long value set for a given key.
     */
    public abstract long getLong(String key);

    /**\
     * @return a long value set for a given key.<br />
     *         If not set, will return the default value provided.
     */
    public long getLong(String key, long def) {
        return !isSet(key) ? def : getLong(key);
    }

}