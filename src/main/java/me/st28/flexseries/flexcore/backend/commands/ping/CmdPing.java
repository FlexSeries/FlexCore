package me.st28.flexseries.flexcore.backend.commands.ping;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.commands.FlexHelpCommand;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class CmdPing extends FlexCommand<FlexCore> {

    public CmdPing(FlexCore plugin) {
        super(
                plugin,
                new String[]{"flexping", "ping"},
                null,
                new FlexCommandSettings<FlexCore>()
                    .description("Server list message modification command")
                    .defaultSubcommand("list")
                    .helpPath("FlexCore.Ping")
                    .description("Live server list changing")
                    .setDummyCommand()
        );

        //TODO: Command to view ping command
        registerSubcommand(new SCmdPingList(plugin, this));
        registerSubcommand(new SCmdPingSet(plugin, this));
        registerSubcommand(new FlexHelpCommand<>(plugin, this));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {}

}