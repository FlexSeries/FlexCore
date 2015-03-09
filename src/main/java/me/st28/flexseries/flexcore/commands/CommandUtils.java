package me.st28.flexseries.flexcore.commands;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.commands.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.messages.ReplacementMap;
import me.st28.flexseries.flexcore.permissions.PermissionNode;
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

}