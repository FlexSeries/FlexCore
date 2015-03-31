package me.st28.flexseries.flexcore.command.exceptions;

import me.st28.flexseries.flexcore.message.MessageReference;
import org.bukkit.command.CommandSender;

public final class CommandInterruptedException extends RuntimeException {

    private MessageReference messageRef;

    public CommandInterruptedException(MessageReference message) {
        super(message.getPlainMessage());
        this.messageRef = message;
    }

    public void sendTo(CommandSender sender) {
        messageRef.sendTo(sender);
    }

}