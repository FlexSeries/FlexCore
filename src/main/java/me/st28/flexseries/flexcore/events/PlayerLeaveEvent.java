package me.st28.flexseries.flexcore.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Universal event for all possible ways a player can leave the server.
 */
public final class PlayerLeaveEvent extends PlayerEvent {

    private final static HandlerList handlerList = new HandlerList();

    private String leaveMessage;
    private Map<UUID, String> overriddenLeaveMessages = new HashMap<>();

    public PlayerLeaveEvent(Player who) {
        super(who);
    }

    public void setLeaveMessage(String message) {
        this.leaveMessage = message;
    }

    public void setLeaveMessage(UUID player, String message) {
        overriddenLeaveMessages.put(player, message);
    }

    public String getLeaveMessage() {
        return leaveMessage;
    }

    public String getLeaveMessage(UUID player) {
        return overriddenLeaveMessages.containsKey(player) ? overriddenLeaveMessages.get(player) : leaveMessage;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}