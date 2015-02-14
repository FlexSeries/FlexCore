package me.st28.flexseries.flexcore.messages;

import org.bukkit.ChatColor;

import java.io.IOException;
import java.util.Map;

public final class InvalidMessageProvider extends MessageProvider {

    private final static String UNKNOWN_MESSAGE = ChatColor.RED + "Unknown message: " + ChatColor.GOLD + "{PATH}";

    InvalidMessageProvider() {
        super(null);
    }

    @Override
    public void reload() throws IOException { }

    @Override
    public String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', UNKNOWN_MESSAGE.replace("{PATH}", path));
    }

    @Override
    public String getMessage(String path, Map<String, String> replacements) {
        return getMessage(path);
    }

}