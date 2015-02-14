package me.st28.flexseries.flexcore.events;

import me.st28.flexseries.flexcore.messages.MessageReference;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Fired when a player is completely loaded and officially joins the server.
 */
public final class PlayerJoinLoadedEvent extends PlayerEvent {

    private static HandlerList handlerList = new HandlerList();

    private Map<String, Object> customData;

    private MessageReference joinMessage;
    private Map<UUID, MessageReference> overriddenJoinMessages = new HashMap<>();

    /**
     * The messages that are to be sent to the newly joined player.<br />
     * The order of the messages sent can be set via the core plugin's configuration file.
     */
    private Map<String, MessageReference> loginMessages = new HashMap<>();

    public PlayerJoinLoadedEvent(Player who, Map<String, Object> customData) {
        super(who);
        this.customData = customData;
    }

    /**
     * @return custom data set for the event.
     */
    public Map<String, Object> getCustomData() {
        return customData;
    }

    /**
     * Sets the default join message.
     *
     * @param message The message to set.
     */
    public void setJoinMessage(MessageReference message) {
        this.joinMessage = message;
    }

    /**
     * Sets the join message for a specified player.
     *
     * @param player The player to set the message for.
     * @param message The message to set for the player.
     */
    public void setJoinMessage(UUID player, MessageReference message) {
        overriddenJoinMessages.put(player, message);
    }

    /**
     * @return the default join message.
     */
    public MessageReference getJoinMessage() {
        return joinMessage;
    }

    /**
     * @return the join message for a specified player.
     */
    public MessageReference getJoinMessage(UUID player) {
        return overriddenJoinMessages.containsKey(player) ? overriddenJoinMessages.get(player) : joinMessage;
    }

    /**
     * @return an unmodifiable view of the login messages.
     */
    public Map<String, MessageReference> getLoginMessages() {
        return Collections.unmodifiableMap(loginMessages);
    }

    /**
     * Adds a login message.
     *
     * @param plugin The plugin that owns the message.
     * @param identifier The identifier for the message.
     * @param message The message to send.
     */
    public void addLoginMessage(Class<? extends JavaPlugin> plugin, String identifier, MessageReference message) {
        loginMessages.put(plugin.getCanonicalName() + "$" + identifier, message);
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}