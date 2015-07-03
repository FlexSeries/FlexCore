package me.st28.flexseries.flexcore.variable;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents a text variable that will be replaced in chat messages by something else.
 */
public abstract class Variable {

    private static final Map<String, Variable> variables = new HashMap<>();

    public static boolean registerVariable(Variable variable) {
        Validate.notNull(variable, "Variable cannot be null.");
        if (variables.containsKey(variable.key)) {
            return false;
        }

        variables.put(variable.key, variable);
        return true;
    }

    public static String handleReplacements(Player player, String input) {
        for (Entry<String, Variable> entry : variables.entrySet()) {
            if (input.contains(entry.getKey())) {
                String replacement = entry.getValue().getReplacement(player);

                if (replacement != null) {
                    input = input.replace(entry.getKey(), replacement);
                }
            }
        }

        return input;
    }

    private String key;

    public Variable(String key) {
        Validate.notNull(key, "Key cannot be null.");
        this.key = "{" + key.toUpperCase() + "}";
    }

    /**
     * @param player The player. Can be null, so this method should handle that appropriately.
     *
     * @return A replacement for the given player.<br />
     *         Null if there is no replacement for the player.
     */
    public abstract String getReplacement(Player player);

}