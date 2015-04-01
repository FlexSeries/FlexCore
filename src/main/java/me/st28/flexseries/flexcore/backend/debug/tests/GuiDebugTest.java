package me.st28.flexseries.flexcore.backend.debug.tests;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.debug.DebugTest;
import me.st28.flexseries.flexcore.debug.DebugTestOutput;
import me.st28.flexseries.flexcore.gui.debug.DebugGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class GuiDebugTest extends DebugTest<FlexCore> {

    public GuiDebugTest(FlexCore plugin) {
        super(plugin, "gui", "Opens a very simple GUI");

        playerOnly = true;
    }

    @Override
    public DebugTestOutput runTest(CommandSender sender, String label, String[] args) {
        Player p = (Player) sender;

        new DebugGui().openForPlayer(p);
        return new DebugTestOutput(true, (String) null);
    }

}