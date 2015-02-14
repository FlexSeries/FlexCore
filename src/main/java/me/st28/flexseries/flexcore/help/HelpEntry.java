package me.st28.flexseries.flexcore.help;

import me.st28.flexseries.flexcore.lists.ListManager;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permissible;

abstract class HelpEntry {

    final String format;
    String permission;

    HelpEntry(String format) {
        this.format = format;
        if (getFormat() == null) throw new IllegalArgumentException("No format with the name '" + format + "' exists.");
    }

    boolean isAvailableFor(Permissible p) {
        return permission == null || p.hasPermission(permission);
    }

    final String getFormat() {
        return FlexPlugin.getRegisteredModule(ListManager.class).getElementFormat(format);
    }

    abstract String getMessage(CommandSender sender);

}