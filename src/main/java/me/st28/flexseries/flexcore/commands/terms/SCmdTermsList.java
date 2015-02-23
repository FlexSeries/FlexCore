package me.st28.flexseries.flexcore.commands.terms;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.commands.CommandUtils;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.lists.ListBuilder;
import me.st28.flexseries.flexcore.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.terms.Terms;
import me.st28.flexseries.flexcore.terms.TermsManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.*;

public final class SCmdTermsList extends FlexCommand<FlexCore> {

    public SCmdTermsList(FlexCore plugin, CmdTerms cmdTerms) {
        super(
                plugin,
                new String[]{"list"},
                cmdTerms,
                new FlexCommandSettings<FlexCore>()
                    .description("Lists all registered Terms")
                    .permission(PermissionNodes.TERMS_LIST),
                new CommandArgument("page", false)
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        int page = CommandUtils.getPage(args, 0);

        TermsManager termsManager = plugin.getModule(TermsManager.class);

        List<Terms> terms = new ArrayList<>(termsManager.getRegisteredTerms());
        Collections.sort(terms, new Comparator<Terms>() {
            @Override
            public int compare(Terms o1, Terms o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        ListBuilder builder = new ListBuilder("page", "Terms", null, label);

        UUID uuid = CommandUtils.getSenderUuid(sender);

        for (Terms curTerms : terms) {
            String prefix = "";
            if (uuid != null) {
                if (termsManager.hasPlayerAgreed(uuid, curTerms)) {
                    prefix = ChatColor.DARK_GRAY + "(" + ChatColor.GREEN + "ACCEPTED" + ChatColor.DARK_GRAY + ") ";
                } else {
                    prefix = ChatColor.DARK_GRAY + "(" + ChatColor.DARK_RED + "DECLINED" + ChatColor.DARK_GRAY + ") ";
                }
            }
            builder.addMessage("title", prefix + curTerms.getName(), curTerms.getDescription());
        }

        builder.sendTo(sender, page);
    }

}