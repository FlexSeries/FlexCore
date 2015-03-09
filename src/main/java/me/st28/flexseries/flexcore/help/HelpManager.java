package me.st28.flexseries.flexcore.help;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.lists.ListBuilder;
import me.st28.flexseries.flexcore.lists.ListManager;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.Map.Entry;

public final class HelpManager extends FlexModule<FlexCore> {

    final Map<String, HelpTopic> helpTopics = new HashMap<>();

    private final String unknownMessage = "" + ChatColor.RED + ChatColor.ITALIC + "Unknown help topic: " + ChatColor.DARK_RED + "{PATH}";

    public HelpManager(FlexCore plugin) {
        super(plugin, "help", "Manages help page generation", false, ListManager.class);

        isDisableable = false;
    }

    public Collection<HelpTopic> getHelpTopics(String partialIdentifier) {
        if (partialIdentifier == null) {
            return Collections.unmodifiableCollection(helpTopics.values());
        }

        partialIdentifier = partialIdentifier.toLowerCase();

        List<HelpTopic> returnList = new ArrayList<>();

        for (Entry<String, HelpTopic> entry : helpTopics.entrySet()) {
            if (entry.getKey().startsWith(partialIdentifier)) {
                returnList.add(entry.getValue());
            }
        }

        return returnList;
    }

    public boolean addHelpTopic(HelpTopic topic) {
        if (helpTopics.containsKey(topic.getIdentifier().toLowerCase())) return false;

        helpTopics.put(topic.getIdentifier().toLowerCase(), topic);
        LogHelper.debug(this, "Added help topic: " + topic.getIdentifier());
        return true;
    }

    public HelpTopic getHelpTopic(String identifier) {
        return helpTopics.get(identifier.toLowerCase());
    }

    public ListBuilder buildMessages(String identifier, CommandSender receiver, String label, String command) {
        HelpTopic topic = identifier == null ? null : helpTopics.get(identifier.toLowerCase());
        if (topic == null) {
            ListBuilder builder = new ListBuilder("title", identifier, null, label);
            builder.addMessage(unknownMessage.replace("{PATH}", identifier == null ? ("" + ChatColor.ITALIC + "(none)") : identifier));
            return builder;
        }

        List<HelpEntry> entries = new ArrayList<>(topic.entries);
        Iterator<HelpEntry> it = entries.iterator();
        while (it.hasNext()) {
            if (!it.next().isAvailableFor(receiver)) {
                it.remove();
            }
        }

        List<HelpTopic> parents = topic.getParents();
        StringBuilder rawHeaderKey = new StringBuilder();
        if (parents.size() > 2) {
            rawHeaderKey
                    .append(ChatColor.DARK_GRAY).append("...")
                    .append(ChatColor.GOLD).append(parents.get(parents.size() - 2).name)
                    .append(ChatColor.DARK_GRAY).append(" / ")
                    .append(ChatColor.GOLD).append(parents.get(parents.size() - 1).name);
        } else {
            for (HelpTopic parent : parents) {
                if (rawHeaderKey.length() > 0) {
                    rawHeaderKey.append(ChatColor.DARK_GRAY).append(" / ");
                }
                rawHeaderKey.append(ChatColor.GOLD).append(parent.name);
            }
        }

        if (rawHeaderKey.length() != 0) {
            rawHeaderKey.append(ChatColor.DARK_GRAY).append(" / ").append(ChatColor.GOLD);
        }
        rawHeaderKey.append(topic.name);

        ListBuilder builder = new ListBuilder("help", rawHeaderKey.toString(), null, label);
        if (command != null) {
            builder.enableNextPageNotice("/" + command + " " + identifier.replace(".", " ") + " {NEXTPAGE}");
        }

        for (HelpEntry cur : entries) {
            builder.addMessage(cur.getMessage(receiver));
        }

        List<HelpTopic> children = new ArrayList<>(topic.getChildren());
        if (!children.isEmpty()) {
            Collections.sort(children, new Comparator<HelpTopic>() {
                @Override
                public int compare(HelpTopic o1, HelpTopic o2) {
                    return o1.name.toLowerCase().compareTo(o2.name.toLowerCase());
                }
            });

            for (HelpTopic child : children) {
                builder.addMessage("title", child.name, child.description == null ? ("" + ChatColor.RED + ChatColor.ITALIC + "No description set.") : child.description);
            }
        }

        return builder;
    }

}