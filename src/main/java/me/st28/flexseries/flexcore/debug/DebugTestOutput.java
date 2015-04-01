package me.st28.flexseries.flexcore.debug;

import me.st28.flexseries.flexcore.message.MessageReference;

/**
 * Represents the output of {@link DebugTest#runTest(org.bukkit.command.CommandSender, String, String[])}.
 */
public final class DebugTestOutput {

    private final boolean isSuccess;
    private final MessageReference message;

    public DebugTestOutput(boolean isSuccess, String message) {
        this(isSuccess, message == null ? null : MessageReference.createPlain(message));
    }

    public DebugTestOutput(boolean isSuccess, MessageReference message) {
        this.isSuccess = isSuccess;
        this.message = message;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public MessageReference getMessage() {
        return message;
    }

}