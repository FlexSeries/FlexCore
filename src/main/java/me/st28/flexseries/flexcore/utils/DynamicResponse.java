package me.st28.flexseries.flexcore.utils;

import me.st28.flexseries.flexcore.messages.MessageReference;
import org.bukkit.command.CommandSender;

/**
 * Represents the response of a method that has a variable message.
 */
public final class DynamicResponse {

    private final boolean isSuccess;
    private final MessageReference[] messages;

    public DynamicResponse(boolean isSuccess, MessageReference... messages) {
        this.isSuccess = isSuccess;
        this.messages = messages;
    }

    public final boolean isSuccess() {
        return isSuccess;
    }

    public MessageReference[] getMessages() {
        return messages;
    }

    public final void sendMessage(CommandSender sender) {
        for (MessageReference message : messages) {
            message.sendTo(sender);
        }
    }

}