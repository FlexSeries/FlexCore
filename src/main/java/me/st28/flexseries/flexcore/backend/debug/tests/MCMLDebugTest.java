package me.st28.flexseries.flexcore.backend.debug.tests;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.command.CommandArgument;
import me.st28.flexseries.flexcore.debug.DebugTest;
import me.st28.flexseries.flexcore.debug.DebugTestOutput;
import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.util.ArrayUtils;
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