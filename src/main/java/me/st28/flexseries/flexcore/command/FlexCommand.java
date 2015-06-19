/**
 * FlexCore - Licensed under the MIT License (MIT)
 *
 * Copyright (c) Stealth2800 <http://stealthyone.com/>
 * Copyright (c) contributors <https://github.com/FlexSeries>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.st28.flexseries.flexcore.command;

import me.st28.flexseries.flexcore.cookie.CookieManager;
import me.st28.flexseries.flexcore.permission.PermissionNode;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import java.util.*;

/**
 * Represents a command that uses FlexCore's command library.
 *
 * @param <T> The plugin that owns the command.
 */
public abstract class FlexCommand<T extends FlexPlugin> {

    /**
     * The plugin that owns this command.
     */
    private final T plugin;

    /**
     * The label(s) for the command.
     */
    private final List<String> labels = new ArrayList<>();

    /**
     * The command that this command exists under.
     */
    private final FlexCommand<T> parent;

    /**
     * The arguments for this command.
     */
    private final List<CommandArgument> arguments = new ArrayList<>();

    /**
     * Subcommands under this command.
     */
    private final Map<String, FlexSubcommand<T>> subcommands = new LinkedHashMap<>();

    /**
     * The settings for this command.
     */
    private final FlexCommandSettings settings;

    /**
     * Instantiates a FlexCommand that is a base command for a plugin.
     */
    public FlexCommand(T plugin, String label, List<CommandArgument> arguments, FlexCommandSettings settings) {
        Validate.notNull(plugin, "Plugin cannot be null.");
        Validate.notNull(label, "Label cannot be null.");

        PluginCommand pluginCommand = plugin.getCommand(label);
        if (pluginCommand == null) {
            throw new IllegalArgumentException("Command '" + label + "' is not a registered command for plugin '" + plugin.getName() + "'");
        }

        this.plugin = plugin;
        this.labels.add(label.toLowerCase());
        this.labels.addAll(pluginCommand.getAliases());
        this.parent = null;
        if (arguments != null) {
            Validate.noNullElements(arguments, "Arguments list cannot contain any null arguments.");
            this.arguments.addAll(arguments);
        }

        if (settings == null) {
            this.settings = new FlexCommandSettings();
        } else {
            this.settings = settings;
        }

        if (pluginCommand.getDescription() != null) {
            this.settings.description(pluginCommand.getDescription());
        }
    }

    /**
     * @param plugin the plugin that owns the command.
     * @param label the main label of the command
     * @param arguments The arguments for the command. Used to build usage messages for FlexCommands.
     */
    public FlexCommand(T plugin, String label, FlexCommand<T> parent, List<CommandArgument> arguments, FlexCommandSettings settings) {
        Validate.notNull(plugin, "Plugin cannot be null.");
        Validate.notNull(label, "Label cannot be null.");

        this.plugin = plugin;
        this.labels.add(label.toLowerCase());
        this.parent = parent;
        if (arguments != null) {
            Validate.noNullElements(arguments, "Arguments list cannot contain any null arguments.");
            this.arguments.addAll(arguments);
        }

        if (settings == null) {
            this.settings = new FlexCommandSettings();
        } else {
            this.settings = settings;
        }
    }

    /**
     * @return the plugin that owns this command.
     */
    public final T getPlugin() {
        return plugin;
    }

    /**
     * @return an unmodifiable list containing the label(s) for this command. Element 0 is the main label.
     */
    public final List<String> getLabels() {
        return Collections.unmodifiableList(labels);
    }

    /**
     * @return the immediate parent for this command.
     */
    public final FlexCommand<T> getParent() {
        return parent;
    }

    /**
     * @return a list containing the hierarchy of parents for this command.
     */
    public final List<FlexCommand<T>> getParents() {
        List<FlexCommand<T>> returnList = new ArrayList<>();

        FlexCommand<T> tempParent = parent;
        while (tempParent != null) {
            returnList.add(tempParent);
            tempParent = tempParent.parent;
        }

        Collections.reverse(returnList);
        return returnList;
    }

    /**
     * @return the {@link PermissionNode} for this command.
     */
    public final PermissionNode getPermission() {
        PermissionNode permission = settings.getPermission();
        if (permission == null && settings.shouldInheritPermission()) {
            return getParent() == null ? null : getParent().getPermission();
        }
        return permission;
    }

    /**
     * @return the arguments for this command, including the parent arguments (where applicable).
     */
    public final List<CommandArgument> getArguments() {
        if (this.parent != null) {
            return parent.getArguments();
        }
        return Collections.unmodifiableList(arguments);
    }

    /**
     * @return the number of required arguments in the list returned by {@link #getArguments()}.
     */
    public final int getRequiredArguments() {
        int count = 0;
        for (CommandArgument argument : arguments) {
            if (argument.isRequired()) {
                count++;
            }
        }
        return count;
    }

    /**
     * @param sender The CommandSender that the usage message will be sent to.
     * @return The usage message for this command.
     */
    public final String buildUsage(CommandSender sender) {
        StringBuilder sb = new StringBuilder("/");

        CookieManager cookieManager = FlexPlugin.getRegisteredModuleSilent(CookieManager.class);

        if (cookieManager == null || sender == null) {
            // No cookies in use, build usage with default labels.

            for (FlexCommand<T> curCommand : getParents()) {
                if (sb.length() > 1) {
                    sb.append(" ");
                }

                sb.append(curCommand.labels.get(0));
            }

            if (sb.length() > 1) {
                sb.append(" ");
            }
            sb.append(labels.get(0)); // Add the label for this command.

            // Add the arguments for this command.
            for (CommandArgument arg : arguments) {
                sb.append(" ").append(arg.toString());
            }
        } else {
            // Cookies in use, use the last used label alias.

            String cookieUserId = CookieManager.getUserIdentifier(sender);

            for (FlexCommand<T> curCommand : getParents()) {
                if (sb.length() > 1) {
                    sb.append(" ");
                }

                sb.append(cookieManager.getValue(cookieUserId, curCommand.labels.get(0), plugin.getClass(), curCommand.getLabelCookieIdentifier()));
            }

            if (sb.length() > 1) {
                sb.append(" ");
            }
            sb.append(cookieManager.getValue(cookieUserId, labels.get(0), plugin.getClass(), getLabelCookieIdentifier())); // Add the label for this command.

            // Add the arguments for this command.
            for (CommandArgument arg : arguments) {
                sb.append(" ").append(arg.toString());
            }
        }

        return sb.toString();
    }

    /**
     * @return an unmodifiable map of the subcommands under this command.
     */
    public final Map<String, FlexSubcommand<T>> getSubcommands() {
        return Collections.unmodifiableMap(subcommands);
    }

    String getLabelCookieIdentifier() {
        return "command-" + labels.get(0) + "-label";
    }

    /**
     * Registers a subcommand underneath this command.
     *
     * @param subcommand The subcommand to register.
     * @param labelAliases Aliases for the main command label that will execute a subcommand directly.
     * @return True if at least one label for the subcommand was registered successfully.
     */
    protected final boolean registerSubcommand(FlexSubcommand<T> subcommand, String... labelAliases) {
        int registeredLabels = 0;

        for (String subLabelAlias : subcommand.getLabels()) {
            subLabelAlias = subLabelAlias.toLowerCase();
            if (!subcommands.containsKey(subLabelAlias)) {
                subcommands.put(subLabelAlias, subcommand);
                registeredLabels++;
            }
        }

        /*for (String alias : labelAliases) {
            settings.subcommandAliases.put(alias, subcommand);
        }*/

        return registeredLabels != 0;
    }

    /**
     * @return the {@link FlexCommandSettings} for this command.
     */
    public final FlexCommandSettings getSettings() {
        return settings;
    }

    /**
     * Handles the execution of the command.
     *
     * @param sender The sender of the command.
     * @param command The full text that was entered by the sender. (ex. <code>/label hello world</code>)
     * @param label The label that was used by the sender. (ex. <code>label</code>)
     * @param args The given arguments for the command.<br />
     *             Arguments entered in quotes will appear as a single entry in the array with the quotes removed.<br />
     * @param parameters Optional flags entered by the sender to change the command behavior.
     */
    public abstract void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters);

}