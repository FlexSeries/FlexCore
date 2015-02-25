package me.st28.flexseries.flexcore.commands;

import me.st28.flexseries.flexcore.help.HelpManager;
import me.st28.flexseries.flexcore.help.HelpTopic;
import me.st28.flexseries.flexcore.lists.ListBuilder;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

public final class FlexHelpCommand<T extends FlexPlugin> extends FlexCommand<T> {

    public FlexHelpCommand(T plugin, FlexCommand<T> parent) {
        this(plugin, new String[]{"help"}, parent);
    }

    public FlexHelpCommand(T plugin, String[] labels, FlexCommand<T> parent) {
        super(
                plugin,
                labels,
                parent,
                new FlexCommandSettings<T>()
                        .setInheritHelpPath()
                        .description("View command help"),
                new CommandArgument("subtopic", false),
                new CommandArgument("page", false));
    }

    @Override
    String getLabelCookieIdentifier() {
        return getParent() != null ? getParent().getLabelCookieIdentifier() + "-help" : super.getLabelCookieIdentifier();
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        HelpManager helpManager = FlexPlugin.getRegisteredModule(HelpManager.class);

        boolean pageSpecified = false;
        int page = 1;

        String identifier;
        if (args.length == 0) {
            identifier = null;
        } else {
            StringBuilder idBuilder = new StringBuilder();

            for (int i = 0; i < args.length; i++) {
                if (i == args.length - 1) {
                    try {
                        page = Integer.parseInt(args[i]);
                        pageSpecified = true;
                        break;
                    } catch (NumberFormatException ex) { }
                }

                if (idBuilder.length() > 0) {
                    idBuilder.append(".");
                }
                idBuilder.append(args[i]);
            }

            if (idBuilder.length() == 0) {
                identifier = null;
            } else {
                identifier = idBuilder.toString().replace(" ", ".").toLowerCase();
            }
        }

        String helpPath = getHelpPath();
        identifier = (helpPath == null ? "" : helpPath) + (identifier == null ? "" : ((helpPath == null ? "" : ".") + identifier));
        if (identifier.isEmpty()) {
            identifier = null;
        }

        List<HelpTopic> topics = new ArrayList<>(helpManager.getHelpTopics(identifier));

        if (topics.size() == 1) {
            helpManager.buildMessages(topics.get(0).getIdentifier(), sender, label, command).sendTo(sender, page);
            return;
        }

        Collections.sort(topics, new Comparator<HelpTopic>() {
            @Override
            public int compare(HelpTopic o1, HelpTopic o2) {
                return o1.getIdentifier().compareTo(o2.getIdentifier());
            }
        });

        ListBuilder builder = new ListBuilder("page", "Help Topics", null, label);
        builder.enableNextPageNotice(label + " " + StringUtils.stringCollectionToString(Arrays.asList(args).subList(0, pageSpecified ? args.length - 1 : args.length)));

        for (HelpTopic topic : topics) {
            String desc = topic.getDescription();
            if (desc == null) {
                desc = "" + ChatColor.RED + ChatColor.ITALIC + "No description set.";
            }
            builder.addMessage("title", topic.getName(), desc);
        }

        builder.sendTo(sender, page);
    }

}