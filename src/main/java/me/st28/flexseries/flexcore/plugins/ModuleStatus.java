package me.st28.flexseries.flexcore.plugins;

/**
 * Represents the status of a {@link me.st28.flexseries.flexcore.plugins.FlexModule}.
 */
public enum ModuleStatus {

    /**
     * The module is enabled.
     */
    ENABLED,

    /**
     * The module is disabled due to an error while loading.
     */
    DISABLED_ERROR,

    /**
     * The module is disabled due to the configuration.
     */
    DISABLED_CONFIG,

    /**
     * The module is disabled due to a missing dependency.
     */
    DISABLED_DEPENDENCY

}