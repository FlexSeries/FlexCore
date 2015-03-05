package me.st28.flexseries.flexcore.notifications;

import org.apache.commons.lang.Validate;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents a type of notification.
 */
public final class NotificationType {

    private final JavaPlugin plugin;
    private final String name;
    private final String identifier;
    private final boolean isDisableable;
    private final NotificationType[] types;

    public NotificationType(JavaPlugin plugin, String name, boolean isDisableable, NotificationType... types) {
        Validate.notNull(plugin, "Plugin cannot be null.");
        Validate.notNull(name, "Name cannot be null.");
        Validate.notEmpty(types, "There must be one or more NotificationType.");

        this.plugin = plugin;
        this.name = name;
        this.identifier = plugin.getName() + "-" + name;
        this.isDisableable = isDisableable;
        this.types = types;
    }

    public final JavaPlugin getPlugin() {
        return plugin;
    }

    public final String getName() {
        return name;
    }

    /**
     * @return the identifier for this type of notification.
     */
    public final String getIdentifier() {
        return identifier;
    }

    /**
     * @return true if this notification type can be disabled (in-game notifications, SMS/email can always be disabled).
     */
    public final boolean isDisableable() {
        return isDisableable;
    }

}