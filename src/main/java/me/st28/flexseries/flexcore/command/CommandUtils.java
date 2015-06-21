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

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.command.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.hook.HookManager;
import me.st28.flexseries.flexcore.hook.hooks.VaultHook;
import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.message.ReplacementMap;
import me.st28.flexseries.flexcore.permission.PermissionNode;
import me.st28.flexseries.flexcore.player.uuid_tracker.PlayerUuidTracker;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import me.st28.flexseries.flexcore.util.QuickMap;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Command-related utility methods.
 */
public final class CommandUtils {

    private CommandUtils() { }

    public static String[] fixArguments(String[] args) {
        List<String> newArgs = new ArrayList<>();

        _main:
        for (int i = 0; i < args.length; i++) {
            String curSegment = args[i];

            if (curSegment.contains("\"")) {
                StringBuilder sb = new StringBuilder();
                for (int i2 = i; i2 < args.length; i2++) {
                    String newSegment = args[i2];
                    if (sb.length() > 0) {
                        sb.append(" ");
                    }
                    sb.append(newSegment);

                    if (i2 == args.length - 1) {
                        newArgs.add(sb.toString().replace("\"", ""));
                        break _main;
                    }

                    if (newSegment.endsWith("\"")) {
                        i = i2;

                        newArgs.add(sb.toString().replace("\"", ""));
                        if (i >= args.length) {
                            break _main;
                        }
                        break;
                    }
                }
            } else {
                newArgs.add(args[i]);
            }
        }

        return newArgs.toArray(new String[newArgs.size()]);
    }

    public static void performPlayerTest(CommandSender sender, boolean mustBePlayer) {
        if (!(sender instanceof Player) && mustBePlayer) {
            throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "general.errors.must_be_player"));
        }
    }

    public static void performPermissionTest(CommandSender sender, PermissionNode permission) {
        if (permission != null && !permission.isAllowed(sender)) {
            throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "general.errors.no_permission"));
        }
    }

    public static void performArgsTest(int givenCount, int requiredCount, MessageReference usage) {
        if (givenCount < requiredCount) {
            throw new CommandInterruptedException(usage != null
                    ? MessageReference.create(FlexCore.class, "lib_command.errors.invalid_usage", new ReplacementMap("{USAGE}", usage.getPlainMessage()).getMap())
                    : MessageReference.create(FlexCore.class, "lib_command.errors.invalid_usage_generic")
            );
        }
    }

    public static int getPage(String[] args, int pageIndex) {
        return getPage(args, pageIndex, false);
    }

    public static int getPage(String[] args, int pageIndex, boolean silent) {
        int page;
        try {
            page = Integer.parseInt(args[pageIndex]);
        } catch (NumberFormatException ex) {
            if (!silent) {
                throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "general.errors.page_must_be_int"));
            }
            return -1;
        } catch (IndexOutOfBoundsException ex) {
            return 1;
        }

        if (page < 1) {
            if (!silent) {
                throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "general.errors.number_invalid_minimum", new ReplacementMap("{ITEM}", "Page").put("{MIN}", "1").getMap()));
            }
            return -1;
        }

        return page;
    }

    public static UUID getSenderUuid(CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getUniqueId();
        } else {
            throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "general.errors.must_be_player"));
        }
    }

    public static Player getSenderPlayer(CommandSender sender) {
        if (sender instanceof Player) {
            return (Player) sender;
        } else {
            throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "general.errors.must_be_player"));
        }
    }

    public static Player getTargetPlayer(CommandSender sender, String name, boolean isNotSender) {
        List<Player> matched = Bukkit.matchPlayer(name);

        if (matched.isEmpty()) {
            throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "general.errors.players_matched_none", new ReplacementMap("{NAME}", name).getMap()));
        }

        Player player;
        if (matched.size() == 1) {
            player = matched.get(0);
        } else {
            throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "general.errors.players_matched_multiple", new ReplacementMap("{NAME}", name).getMap()));
        }

        if (isNotSender && sender instanceof Player && getSenderPlayer(sender) == player) {
            throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "general.errors.players_cannot_be_self"));
        }

        return player;
    }

    /**
     * Gets a player's UUID based on name.
     *
     * @param sender The sender of the command.
     * @param name The name entered by the sender.
     * @param notSender True to make sure the target is not the sender.
     * @return The UUID of the matched player.<br />
     *         Null if no matching player was found.
     */
    public static UUID getPlayerUuid(CommandSender sender, String name, boolean notSender, boolean isOnline, boolean silent) {
        UUID found = null;

        PlayerUuidTracker uuidTracker = FlexPlugin.getRegisteredModule(PlayerUuidTracker.class);

        // 1) Try getting by exact name
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) {
            found = online.getUniqueId();
        }

        if (found == null) {
            // 2) Try getting by matching name
            List<String> matchedNames = new ArrayList<>();

            for (String curName : uuidTracker.getNamesToUuids().keySet()) {
                if (name.equalsIgnoreCase(curName)) {
                    // Exact match
                    matchedNames.add(name);
                    break;
                }

                if (curName.toLowerCase().contains(name.toLowerCase())) {
                    // Partial match
                    matchedNames.add(curName);
                }
            }

            if (matchedNames.size() == 1) {
                found = uuidTracker.getUuid(matchedNames.get(0));
                name = uuidTracker.getName(found);
            }
        }

        if (found != null && notSender && sender instanceof Player && ((Player) sender).getUniqueId().equals(found)) {
            if (!silent) {
                throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "general.errors.players_cannot_be_self"));
            }
            return null;
        } else if (found != null && isOnline && Bukkit.getPlayer(found) == null) {
            if (!silent) {
                throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "general.errors.players_matched_offline", new QuickMap<>("{NAME}", name).getMap()));
            }
            return null;
        } else if (found == null) {
            if (!silent) {
                throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "general.errors.players_matched_none_offline", new QuickMap<>("{NAME}", name).getMap()));
            }
        }
        return found;
    }

    /**
     * Attempts to take money from a player. Will stop the command if they don't have enough money.
     */
    public static void attemptPayment(CommandSender sender, double price) {
        if (price <= 0D || !(sender instanceof Player)) {
            return;
        }

        Economy economy = FlexPlugin.getRegisteredModule(HookManager.class).getHook(VaultHook.class).getEconomy();

        if (economy.getBalance((Player) sender, null) < price) {
            throw new CommandInterruptedException(MessageReference.create(FlexCore.class, "general.errors.not_enough_money"));
        }
        economy.withdrawPlayer((Player) sender, null, price);
    }

}