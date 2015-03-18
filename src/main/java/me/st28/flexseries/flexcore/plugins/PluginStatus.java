package me.st28.flexseries.flexcore.plugins;

/**
 * Represents the status of a {@link me.st28.flexseries.flexcore.plugins.FlexPlugin}.
 */
public enum PluginStatus {

    /**
     * The plugin has begun the loading process.
     */
    LOADING,

    /**
     * The plugin has begun the enabling process.
     */
    ENABLING,

    /**
     * The plugin has loaded
     */
    LOADED_ERROR,

    /**
     * The plugin is enabled.
     */
    ENABLED,

    /**
     * The plugin has begun the disabling process.
     */
    DISABLING

}