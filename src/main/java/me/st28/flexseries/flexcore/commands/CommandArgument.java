package me.st28.flexseries.flexcore.commands;

public final class CommandArgument {

    String label;
    boolean isRequired;

    public CommandArgument(String label, boolean isRequired) {
        this.label = label;
        this.isRequired = isRequired;
    }

    @Override
    public String toString() {
        return (isRequired ? "<" : "[") + label + (isRequired ? ">" : "]");
    }

    public String getLabel() {
        return label;
    }

    public boolean isRequired() {
        return isRequired;
    }

}