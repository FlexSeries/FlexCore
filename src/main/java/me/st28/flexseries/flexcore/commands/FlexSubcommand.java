package me.st28.flexseries.flexcore.commands;

import me.st28.flexseries.flexcore.plugins.FlexPlugin;

import java.util.List;

/**
 * Represents a subcommand to another {@link FlexCommand}.
 *
 * @param <T> The plugin that owns the command.
 */
public abstract class FlexSubcommand<T extends FlexPlugin> extends FlexCommand<T> {

    public FlexSubcommand(FlexCommand<T> parent, String label, List<CommandArgument> arguments, FlexCommandSettings settings) {
        super(parent.getPlugin(), label, parent, arguments, settings);
    }

}