package me.st28.flexseries.flexcore.help;

import java.util.*;

public final class HelpTopic {

    /**
     * The path to the topic.<br />
     * Ex. topic.subtopic.subsubtopic
     */
    private final String identifier;

    /**
     * The name of the topic.<br />
     * Ex. Subtopic
     */
    final String name;

    /**
     * The description of the topic.
     */
    final String description;

    /**
     * The parent topic. Null if there is no parent.
     */
    private final HelpTopic parent;

    /**
     * Children under this topic. Empty if there aren't any children.
     */
    private final Map<String, HelpTopic> children = new HashMap<>();

    List<HelpEntry> entries = new ArrayList<>();

    public HelpTopic(String name, String description, HelpTopic parent) {
        this.name = name;
        this.description = description;
        this.parent = parent;
        if (parent != null) {
            StringBuilder builder = new StringBuilder();
            List<String> parentNames = new ArrayList<>();

            HelpTopic curParent = parent;
            while (curParent != null) {
                parentNames.add(curParent.name);
                curParent = curParent.parent;
            }

            for (int i = parentNames.size() - 1; i >= 0; i--) {
                builder.append(parentNames.get(i)).append(".");
            }
            builder.append(name);

            identifier = builder.toString().toLowerCase();

            parent.children.put(name.toLowerCase(), this);
        } else {
            identifier = name.toLowerCase();
        }
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    public final String getIdentifier() {
        return identifier;
    }

    public void addEntry(HelpEntry entry) {
        entries.add(entry);
    }

    public HelpTopic getParent() {
        return parent;
    }

    public List<HelpTopic> getParents() {
        List<HelpTopic> returnList = new ArrayList<>();

        HelpTopic curParent = parent;
        while (curParent != null) {
            returnList.add(curParent);
            curParent = curParent.parent;
        }

        Collections.reverse(returnList);
        return returnList;
    }

    public Collection<HelpTopic> getChildren() {
        return Collections.unmodifiableCollection(children.values());
    }

}