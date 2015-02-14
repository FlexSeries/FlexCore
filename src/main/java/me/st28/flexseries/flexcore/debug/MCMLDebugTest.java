package me.st28.flexseries.flexcore.debug;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.utils.ArrayUtils;
import org.bukkit.command.CommandSender;

public final class MCMLDebugTest extends DebugTest<FlexCore> {

    public MCMLDebugTest(FlexCore plugin) {
        super(plugin, "mcml", "Tests the MCML parser", new CommandArgument("anything...", true));
    }

    @Override
    public DebugTestOutput runTest(CommandSender sender, String label, String[] args) {
        return new DebugTestOutput(true, MessageReference.createPlain(ArrayUtils.stringArrayToString(args, " ")));
    }

}