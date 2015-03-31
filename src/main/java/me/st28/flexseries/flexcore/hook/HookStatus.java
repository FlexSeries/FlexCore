package me.st28.flexseries.flexcore.hook;

/**
 * Represents the status of a {@link Hook}
 */
public enum HookStatus {

    /**
     * The hook is enabled.
     */
    ENABLED,

    /**
     * The hook is disabled due to an error while loading.
     */
    DISABLED_ERROR,

    /**
     * The hook is disabled due to the configuration.
     */
    DISABLED_CONFIG,

    /**
     * The hook is disabled due to a missing dependency.
     */
    DISABLED_DEPENDENCY

}