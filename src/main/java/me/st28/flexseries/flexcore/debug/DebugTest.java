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
package me.st28.flexseries.flexcore.debug;

import me.st28.flexseries.flexcore.command.CommandArgument;
import me.st28.flexseries.flexcore.permission.PermissionNode;
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