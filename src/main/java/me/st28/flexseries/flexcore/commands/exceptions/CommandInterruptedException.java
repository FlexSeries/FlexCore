package me.st28.flexseries.flexcore.commands.exceptions;

import me.st28.flexseries.flexcore.messages.MessageReference;
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