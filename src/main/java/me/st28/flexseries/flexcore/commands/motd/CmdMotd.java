package me.st28.flexseries.flexcore.commands.motd;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import org.bukkit.command.CommandSender;

public final class CmdMotd extends FlexCommand<FlexCore> {

    public CmdMotd(FlexCore plugin) {
        super(
                plugin,
                new String[]{"flexmotd", "motd"},
                null,
                new FlexCommandSettings<FlexCore>()
                    .description("MOTD modification command")
                    .defaultSubcommand("list")
                    .setDummyCommand()
        );

        registerSubcommand(new SCmdMotdList(plugin, this));
        registerSubcommand(new SCmdMotdSet(plugin, this));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args) { }

}