package me.st28.flexseries.flexcore.command;

import me.st28.flexseries.flexcore.permission.PermissionNode;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import org.apache.commons.lang.Validate;

import java.util.HashMap;
import java.util.Map;

public final class FlexCommandSettings<T extends FlexPlugin> {

    private boolean isLocked = false;

    /**
     * The description of the command.
     */
    private String description = null;

    /**
     * If true, only players can use this command.
     */
    private boolean isPlayerOnly = false;

    /**
     * The default subcommand to use.<br />
     * If not specified, #runCommand should do something for this command.
     */
    private String defaultSubcommand;

    /**
     * Label aliases that execute subcommands directly.
     */
    private final Map<String, FlexCommand<T>> subcommandAliases = new HashMap<>();

    /**
     * If true, will use the default subcommand, regardless of argument count.
     */
    private boolean isDummyCommand = false;

    /**
     * The permission node required to use this command.
     */
    private PermissionNode permission = null;

    /**
     * If true, will inherit the parent permission node if {@link #permission} is null.
     */
    private boolean shouldInheritPermission = true;

    /**
     * Locks the settings instance to prevent modification.
     */
    public void lock() {
        checkState();

        this.isLocked = true;
    }

    /**
     * @return true if this settings instance cannot be modified any longer.
     */
    public final boolean isLocked() {
        return isLocked;
    }

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

    /**
     * @return the description for this command.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Makes this command only usable by players.
     *
     * @return The settings instance, for chaining.
     */
    public final FlexCommandSettings<T> setPlayerOnly(boolean isPlayerOnly) {
        checkState();

        this.isPlayerOnly = isPlayerOnly;
        return this;
    }

    /**
     * @return true if this command can only be run by players.
     */
    public final boolean isPlayerOnly() {
        return isPlayerOnly;
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
     * @return the default subcommand.
     */
    public final String getDefaultSubcommand() {
        return defaultSubcommand;
    }

    /**
     * Makes this command a dummy command, meaning that it will use the default subcommand, regardless of argument count.
     *
     * @return The settings instance, for chaining.
     */
    public final FlexCommandSettings<T> setDummyCommand(boolean isDummyCommand) {
        checkState();

        this.isDummyCommand = isDummyCommand;
        return this;
    }

    public final boolean isDummyCommand() {
        return isDummyCommand;
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
     * @return the {@link PermissionNode} required to use this command.<br />
     *         In most cases, {@link FlexCommand#getPermission()} should be used over this.
     */
    public final PermissionNode getPermission() {
        return permission;
    }

    /**
     * Sets whether this command inherits the parent permission.
     *
     * @return The settings instance, for chaining.
     */
    public final FlexCommandSettings<T> shouldInheritPermission(boolean shouldInheritPermission) {
        checkState();

        this.shouldInheritPermission = shouldInheritPermission;
        return this;
    }

    /**
     * @return true if this command inherits the parent permission.
     */
    public final boolean shouldInheritPermission() {
        return shouldInheritPermission;
    }

}