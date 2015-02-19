package me.st28.flexseries.flexcore.events;

import me.st28.flexseries.flexcore.messages.MessageReference;
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

    private MessageReference leaveMessage;
    private Map<UUID, MessageReference> overriddenLeaveMessages = new HashMap<>();

    public PlayerLeaveEvent(Player who) {
        super(who);
    }

    public void setLeaveMessage(MessageReference message) {
        this.leaveMessage = message;
    }

    public void setLeaveMessage(UUID player, MessageReference message) {
        overriddenLeaveMessages.put(player, message);
    }

    public MessageReference getLeaveMessage() {
        return leaveMessage;
    }

    public MessageReference getLeaveMessage(UUID player) {
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