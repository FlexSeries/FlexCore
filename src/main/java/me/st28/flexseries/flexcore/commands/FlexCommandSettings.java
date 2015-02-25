package me.st28.flexseries.flexcore.commands;

import me.st28.flexseries.flexcore.permissions.PermissionNode;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import org.apache.commons.lang.Validate;

import java.util.HashMap;
import java.util.Map;

public final class FlexCommandSettings<T extends FlexPlugin> {

    boolean isLocked = false;

    /**
     * The description of the command.
     */
    String description = null;

    /**
     * If true, only players can use this command.
     */
    boolean isPlayerOnly = false;

    /**
     * The default subcommand to use.<br />
     * If not specified, #runCommand should do something for this command.
     */
    String defaultSubcommand;

    /**
     * Label aliases that execute subcommands directly.
     */
    final Map<String, FlexCommand<T>> subcommandAliases = new HashMap<>();

    /**
     * If true, will use the default subcommand, regardless of argument count.
     */
    boolean isDummyCommand = false;

    /**
     * The permission node required to use this command.
     */
    PermissionNode permission = null;

    /**
     * If true, will inherit the parent permission node, where applicable.
     */
    boolean shouldInheritPermission = false;

    /**
     * If true, will inherit the parent help path.
     */
    boolean shouldInheritHelpPath = false;

    /**
     * The path of the help topic for this command.
     */
    String helpPath = null;

    private void checkState() {
        if (isLocked) {
            throw new IllegalStateException("This command settings instance is locked.");
        }
    }

    /**
     * Sets the description for this command.
     *
     * @return The settings instance, for chaining.
     */
    public final FlexCommandSettings<T> description(String description) {
        Validate.notNull(description, "Description must not be null.");
        checkState();

        this.description = description;
        return this;
    }

    public final String getDescription() {
        return description;
    }

    /**
     * Makes this command only usable by players.
     *
     * @return The settings instance, for chaining.
     */
    public final FlexCommandSettings<T> setPlayerOnly() {
        checkState();

        this.isPlayerOnly = true;
        return this;
    }

    /**
     * Sets the default subcommand for this command.
     *
     * @return The settings instance, for chaining.
     */
    public final FlexCommandSettings<T> defaultSubcommand(String defaultSubcommand) {
        Validate.notNull(defaultSubcommand, "Default subcommand must not be null.");
        checkState();

        this.defaultSubcommand = defaultSubcommand;
        return this;
    }

    /**
     * Makes this command a dummy command, meaning that it will use the default subcommand, regardless of argument count.
     *
     * @return The settings instance, for chaining.
     */
    public final FlexCommandSettings<T> setDummyCommand() {
        checkState();

        this.isDummyCommand = true;
        return this;
    }

    /**
     * Sets the permission node required to use this command.
     *
     * @return The settings instance, for chaining.
     */
    public final FlexCommandSettings<T> permission(PermissionNode permission) {
        Validate.notNull(permission, "Permission must not be null.");
        checkState();

        this.permission = permission;
        return this;
    }

    /**
     * Makes this command inherit permissions from parent commands.
     *
     * @return The settings instance, for chaining.
     */
    public final FlexCommandSettings<T> setInheritPermission() {
        checkState();

        this.shouldInheritPermission = true;
        return this;
    }

    /**
     * Makes this command inherit the parent help path, where applicable.
     *
     * @return The settings instance, for chaining.
     */
    public final FlexCommandSettings<T> setInheritHelpPath() {
        checkState();

        this.shouldInheritHelpPath = true;
        return this;
    }

    /**
     * Sets the path for the help topic for this command.
     *
     * @return The settings instance, for chaining.
     */
    public final FlexCommandSettings<T> helpPath(String helpPath) {
        Validate.notNull(helpPath, "Help path must not be null.");
        checkState();

        this.helpPath = helpPath;
        return this;
    }

}