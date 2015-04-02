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

import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.util.ArrayUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public final class FlexTabCompleterWrapper implements TabCompleter {

    private FlexCommand<?> command;

    public FlexTabCompleterWrapper(FlexCommand<?> command) {
        this.command = command;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        try {
            args = CommandUtils.fixArguments(args);
            FlexCommand<?> currentCommand = this.command.getSubcommands().get(label.toLowerCase());;

            if (currentCommand == null) {
                currentCommand = this.command;
            }

            int curIndex = 0;
            while (args.length > curIndex) {
                FlexCommand subcommand = currentCommand.getSubcommands().get(args[curIndex].toLowerCase());

                if (subcommand != null) {
                    currentCommand = subcommand;
                    curIndex++;
                } else {
                    break;
                }
            }

            FlexCommandSettings commandSettings = currentCommand.getSettings();

            CommandUtils.performPlayerTest(sender, commandSettings.isPlayerOnly());
            CommandUtils.performPermissionTest(sender, currentCommand.getPermission());

            String[] fullNewArgs;
            if (args.length < curIndex) {
                fullNewArgs = args;
            } else {
                fullNewArgs = CommandUtils.fixArguments(ArrayUtils.stringArraySublist(args, curIndex, args.length));
            }

            List<String> newArgs = new ArrayList<>();
            for (String newArg : fullNewArgs) {
                Matcher matcher = FlexCommandWrapper.PARAMETER_PATTERN.matcher(newArg);
                if (matcher.matches()) {
                    continue;
                } else {
                    matcher = FlexCommandWrapper.PARAMETER_VALUE_PATTERN.matcher(newArg);
                    if (matcher.matches()) {
                        continue;
                    }
                }

                newArgs.add(newArg);
            }

            if (commandSettings.isDummyCommand() || newArgs.size() < currentCommand.getRequiredArguments()) {
                String defCommand = commandSettings.getDefaultSubcommand();
                if (defCommand != null) {
                    FlexCommand<?> defaultCommand = currentCommand.getSubcommands().get(defCommand);

                    if (!defaultCommand.getClass().equals(FlexHelpCommand.class)) {
                        currentCommand = currentCommand.getSubcommands().get(defCommand);

                        CommandUtils.performPlayerTest(sender, commandSettings.isPlayerOnly());
                        CommandUtils.performPermissionTest(sender, currentCommand.getPermission());
                        CommandUtils.performArgsTest(newArgs.size(), currentCommand.getRequiredArguments(), MessageReference.createPlain(currentCommand.buildUsage(sender)));
                    }
                }
            }

            if (currentCommand instanceof FlexTabCompleter) {
                return ((FlexTabCompleter) currentCommand).getTabOptions(sender, newArgs.toArray(new String[newArgs.size()]));
            }
        } catch (Exception ex) {}
        return null;
    }

}