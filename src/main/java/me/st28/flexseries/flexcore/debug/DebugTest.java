package me.st28.flexseries.flexcore.debug;

import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.permissions.PermissionNode;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class DebugTest<T extends JavaPlugin> {

    public final static String DESCRIPTION_DEFAULT = "" + ChatColor.RED + ChatColor.ITALIC + "No description set.";

    protected final T plugin;

    private final String name;
    private final String description;

    protected boolean playerOnly = false;
    protected PermissionNode permission = null;
    private int requiredArgs = 0;
    private final List<CommandArgument> arguments = new ArrayList<>();

    public DebugTest(T plugin, String name, String description, CommandArgument... arguments) {
        this.plugin = plugin;

        Collections.addAll(this.arguments, arguments);
        for (CommandArgument argument : arguments) {
            if (argument.isRequired()) {
                requiredArgs++;
            }
        }

        this.name = name;
        this.description = description;
    }

    public final T getPlugin() {
        return plugin;
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description != null ? description : DESCRIPTION_DEFAULT;
    }

    public final int getRequiredArgs() {
        return requiredArgs;
    }

    public final boolean isPlayerOnly() {
        return playerOnly;
    }

    public final PermissionNode getPermission() {
        return permission;
    }

    public final String buildUsage() {
        StringBuilder sb = new StringBuilder();

        for (CommandArgument arg : arguments) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(arg.toString());
        }

        return sb.toString();
    }

    /**
     * Runs the debug test.<br />
     * If an error is encountered during the test, should throw an exception or return null.
     *
     * @return The output of the test.
     */
    public abstract DebugTestOutput runTest(CommandSender sender, String label, String[] args);

}