package me.st28.flexseries.flexcore.notifications;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import org.apache.commons.lang.Validate;

import java.util.HashMap;
import java.util.Map;

public final class NotificationManager extends FlexModule<FlexCore> {

    private final Map<String, NotificationType> notificationTypes = new HashMap<>();

    public NotificationManager(FlexCore plugin) {
        super(plugin, "notifications", "Handles player notifications");
    }

    public final boolean registerNotificationType(NotificationType type) {
        Validate.notNull(type, "Type cannot be null.");

        String identifier = type.getIdentifier();
        if (notificationTypes.containsKey(identifier)) {
            return false;
        }

        notificationTypes.put(identifier, type);
        LogHelper.info(this, "Registered notification type '" + type.getIdentifier() + "'");
        return true;
    }

}