package me.st28.flexseries.flexcore.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Fired when a new player joins the server.
 */
public final class NewPlayerJoinEvent extends PlayerEvent {

    private static HandlerList handlerList = new HandlerList();

    public NewPlayerJoinEvent(Player who) {
        super(who);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}