package me.st28.flexseries.flexcore.events;

import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a {@link FlexPlugin} implementation is reloaded.
 */
public final class PluginReloadedEvent extends Event {

    private static HandlerList handlerList = new HandlerList();

    private final Class<? extends FlexPlugin> plugin;

    public PluginReloadedEvent(Class<? extends FlexPlugin> plugin) {
        this.plugin = plugin;
    }

    public Class<? extends FlexPlugin> getPlugin() {
        return plugin;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}