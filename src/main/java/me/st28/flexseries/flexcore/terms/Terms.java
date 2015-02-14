package me.st28.flexseries.flexcore.terms;

/**
 * Represents terms that a player must agree with to use certain features offered by the server.
 */
public final class Terms {

    final String name;
    final String description;

    final String[] content;

    public Terms(String name, String description, String... content) {
        this.name = name;
        this.description = description;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String[] getContent() {
        return content;
    }

}