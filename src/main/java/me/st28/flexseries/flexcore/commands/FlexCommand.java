package me.st28.flexseries.flexcore.commands;

import me.st28.flexseries.flexcore.cookies.CookieManager;
import me.st28.flexseries.flexcore.help.CookieCommandHelpEntry;
import me.st28.flexseries.flexcore.help.HelpManager;
import me.st28.flexseries.flexcore.help.HelpTopic;
import me.st28.flexseries.flexcore.permissions.PermissionNode;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Represents a command that uses FlexCore's command library.
 *
 * @param <T> The plugin that owns the command.
 */
//TODO: If the base command, should read label aliases from the plugin.yml
public abstract class FlexCommand<T extends FlexPlugin> {

    /**
     * The plugin that owns this command.
     */
    protected final T plugin;

    /**
     * The default labels for the command.
     */
    final String[] labels;

    /**
     * The parent command to this subcommand.
     */
    private final FlexCommand<T> parent;

    /**
     * Subcommands for this particular command.
     */
    final Map<String, FlexCommand<T>> subcommands = new HashMap<>();

    private final List<CommandArgument> arguments = new ArrayList<>();

    protected final FlexCommandSettings<T> settings;

    public FlexCommand(T plugin, String label, FlexCommandSettings<T> settings, CommandArgument... arguments) {
        Validate.notNull(plugin, "Plugin cannot be null.");
        Validate.notNull(label, "Label cannot be null.");

        this.plugin = plugin;

        PluginCommand pluginCommand = plugin.getCommand(label);
        if (pluginCommand == null) {
            throw new IllegalArgumentException("Command '" + label + "' is not a registered command for plugin '" + plugin.getName() + "'");
        }

        List<String> rawLabels = new ArrayList<>();
        rawLabels.add(label.toLowerCase());
        rawLabels.addAll(pluginCommand.getAliases());
        this.labels = rawLabels.toArray(new String[rawLabels.size()]);

        if (settings == null) {
            this.settings = new FlexCommandSettings<>();
        } else {
            this.settings = settings;
        }

        String plDescription = pluginCommand.getDescription();
        if (plDescription != null) {
            this.settings.description(plDescription);
        }
        this.settings.isLocked = true;

        Collections.addAll(this.arguments, arguments);

        this.parent = null;

        String helpPath = getHelpPath();
        if (helpPath != null) {
            HelpManager helpManager = FlexPlugin.getRegisteredModule(HelpManager.class);

            if (getHelpTopic() == null) {
                HelpTopic helpTopic;

                helpTopic = new HelpTopic(helpPath, this.settings.description, null);

                helpManager.addHelpTopic(helpTopic);
            }
        }
    }

    /**
     * Creates a new FlexCommand.
     *
     * @param plugin The plugin that owns the command.
     * @param labels The labels for the command.  <code>labels[0]</code> should be the primary label.
     * @param settings The optional settings for this command.  Can be null.
     */
    public FlexCommand(T plugin, String[] labels, FlexCommand<T> parent, FlexCommandSettings<T> settings, CommandArgument... arguments) {
        Validate.notNull(plugin, "Plugin cannot be null.");
        Validate.notNull(labels, "Labels cannot be null.");
        Validate.notEmpty(labels, "There must be at least one label.");

        this.plugin = plugin;
        this.labels = labels;
        this.parent = parent;

        if (settings == null) {
            this.settings = new FlexCommandSettings<>();
        } else {
            this.settings = settings;
        }
        this.settings.isLocked = true;

        Collections.addAll(this.arguments, arguments);

        String helpPath = getHelpPath();
        if (helpPath != null) {
            HelpManager helpManager = FlexPlugin.getRegisteredModule(HelpManager.class);

            if (getHelpTopic() == null) {
                HelpTopic helpTopic;

                if (this.settings.shouldInheritHelpPath && parent != null) {
                    helpTopic = new HelpTopic(
                            helpPath,
                            this.settings.description,
                            helpManager.getHelpTopic(parent.getHelpPath())
                    );
                } else {
                    helpTopic = new HelpTopic(helpPath, this.settings.description, null);
                }

                helpManager.addHelpTopic(helpTopic);
            }
        }
    }

    public final FlexCommandSettings<T> getSettings() {
        return settings;
    }

    public final String[] getLabels() {
        return labels;
    }

    String getLabelCookieIdentifier() {
        return getClass().getCanonicalName() + "-label";
    }

    public final Map<String, FlexCommand<T>> getSubcommands() {
        return Collections.unmodifiableMap(subcommands);
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
     * @return the arguments for this command, including the parent arguments (where applicable).
     */
    public final List<CommandArgument> getArguments() {
        if (this.parent != null) {
            return parent.getArguments();
        }
        return Collections.unmodifiableList(arguments);
    }

    public final String buildUsage(CommandSender sender) {
        StringBuilder sb = new StringBuilder();

        Player cookieOwner = sender instanceof Player ? (Player) sender : null;
        CookieManager cookieManager = FlexPlugin.getRegisteredModule(CookieManager.class);

        List<FlexCommand<T>> hierarchy = getParents();

        for (FlexCommand<T> curCommand : hierarchy) {
            if (sb.length() == 0) {
                sb.append("/");
            } else {
                sb.append(" ");
            }

            sb.append(cookieManager.getValue(cookieOwner, curCommand.labels[0], plugin.getClass(), curCommand.getLabelCookieIdentifier()));
        }

        if (sb.length() == 0) {
            sb.append("/");
        } else {
            sb.append(" ");
        }
        sb.append(cookieManager.getValue(cookieOwner, labels[0], plugin.getClass(), getLabelCookieIdentifier()));

        for (CommandArgument arg : arguments) {
            sb.append(" ").append(arg.toString());
        }

        return sb.toString();
    }

    /**
     * @return the number of required arguments.
     */
    public final int getRequiredArgs() {
        int count = 0;
        for (CommandArgument argument : arguments) {
            if (argument.isRequired) {
                count++;
            }
        }
        return count;
    }

    /**
     * @return the permission node for this command.
     */
    public PermissionNode getPermissionNode() {
        if (settings.permission == null && this.parent != null && settings.shouldInheritPermission) {
            return parent.getPermissionNode();
        }
        return settings.permission;
    }

    /**
     * @return the help path for this command, including the parent path (where applicable).
     */
    public final String getHelpPath() {
        if (this.parent != null && settings.shouldInheritHelpPath) {
            if (settings.helpPath == null) {
                return parent.getHelpPath();
            } else {
                return parent.getHelpPath() + "." + settings.helpPath;
            }
        }
        return settings.helpPath;
    }

    public final HelpTopic getHelpTopic() {
        String path = getHelpPath();
        return path == null ? null : FlexPlugin.getRegisteredModule(HelpManager.class).getHelpTopic(path);
    }

    /**
     * Registers a subcommand underneath this command.
     *
     * @param subcommand The subcommand to register.
     * @param labelAliases Aliases for the main command label that will execute a subcommand directly.
     * @return True if at least one label for the subcommand was registered successfully.
     */
    protected final boolean registerSubcommand(FlexCommand<T> subcommand, String... labelAliases) {
        int registeredLabels = 0;

        for (String subLabelAlias : subcommand.labels) {
            subLabelAlias = subLabelAlias.toLowerCase();
            if (!subcommands.containsKey(subLabelAlias)) {
                subcommands.put(subLabelAlias, subcommand);
                registeredLabels++;
            }
        }

        for (String alias : labelAliases) {
            settings.subcommandAliases.put(alias, subcommand);
        }

        HelpTopic helpTopic = getHelpTopic();
        if (helpTopic != null) {
            //TODO: Make sure that the subcommand's help topic is the same, if not, create a subtopic
            PermissionNode permission = subcommand.settings.permission;
            String description = subcommand.settings.description;
            if (description == null) {
                description = "&c&oNo description set.";
            }
            //helpTopic.addEntry(new CommandHelpEntry(subcommand.buildUsage(null).replace("/", ""), description, permission == null ? null : permission.getNode()));
            helpTopic.addEntry(new CookieCommandHelpEntry(subcommand));
        }

        return registeredLabels != 0;
    }

    /**
     * Handles the execution of the command.
     *  @param sender The sender of the command.
     * @param command The full text that was entered by the sender.
     * @param label The label that was used by the sender.
     * @param args The {@link CommandArgument}s that were detected for the command.<br />
     *             Arguments entered in quotes will appear as a single entry in the array.<br />
     * @param parameters Optional flags that can be entered by the sender to change the command behavior, where supported.
     */
    public abstract void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters);

}