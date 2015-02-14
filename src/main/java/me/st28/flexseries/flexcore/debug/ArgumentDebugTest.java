package me.st28.flexseries.flexcore.debug;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.commands.CommandUtils;
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