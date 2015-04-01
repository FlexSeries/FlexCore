package me.st28.flexseries.flexcore.backend.debug.tests;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.command.CommandArgument;
import me.st28.flexseries.flexcore.command.CommandUtils;
import me.st28.flexseries.flexcore.debug.DebugTest;
import me.st28.flexseries.flexcore.debug.DebugTestOutput;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public final class ArgumentDebugTest extends DebugTest<FlexCore> {

    public ArgumentDebugTest(FlexCore plugin) {
        super(plugin, "argfix", "Tests the CommandUtils.fixArguments method.", new CommandArgument("anything...", true));

        playerOnly = false;
    }

    @Override
    public DebugTestOutput runTest(CommandSender sender, String label, String[] args) {
        return new DebugTestOutput(true, Arrays.asList(CommandUtils.fixArguments(args)).toString());
    }

}