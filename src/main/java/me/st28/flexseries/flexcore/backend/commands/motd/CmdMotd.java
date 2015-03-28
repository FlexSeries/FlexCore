package me.st28.flexseries.flexcore.backend.commands.motd;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.commands.FlexHelpCommand;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class CmdMotd extends FlexCommand<FlexCore> {

    public CmdMotd(FlexCore plugin) {
        super(
                plugin,
                new String[]{"flexmotd", "motd"},
                null,
                new FlexCommandSettings<FlexCore>()
                    .description("MOTD modification command")
                    .defaultSubcommand("list")
                    .helpPath("FlexCore.Motd")
                    .description("Live in-game MOTD changing")
                    .setDummyCommand()
        );

        registerSubcommand(new SCmdMotdList(plugin, this));
        registerSubcommand(new SCmdMotdSet(plugin, this));
        registerSubcommand(new FlexHelpCommand<>(plugin, this));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) { }

}