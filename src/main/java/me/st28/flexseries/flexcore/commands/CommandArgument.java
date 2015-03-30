package me.st28.flexseries.flexcore.commands;

/**
 * Represents an argument for a command.
 */
public final class CommandArgument {

    private final String label;
    private final boolean isRequired;

    public CommandArgument(String label, boolean isRequired) {
        this.label = label;
        this.isRequired = isRequired;
    }

    @Override
    public String toString() {
        return (isRequired ? "<" : "[") + label + (isRequired ? ">" : "]");
    }

    /**
     * @return the label of the argument.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return true if the argument is required.
     */
    public boolean isRequired() {
        return isRequired;
    }

}