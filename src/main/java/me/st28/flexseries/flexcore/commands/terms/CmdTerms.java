package me.st28.flexseries.flexcore.commands.terms;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.lists.ListBuilder;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.messages.ReplacementMap;
import me.st28.flexseries.flexcore.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.terms.Terms;
import me.st28.flexseries.flexcore.terms.TermsManager;
import me.st28.flexseries.flexcore.utils.ArrayUtils;
import org.bukkit.command.CommandSender;

import java.util.Map;

public final class CmdTerms extends FlexCommand<FlexCore> {

    public CmdTerms(FlexCore plugin) {
        super(
                plugin,
                new String[]{"flexterms", "terms", "tos"},
                null,
                new FlexCommandSettings<FlexCore>()
                        .description("Base command for Terms on the server")
                        .permission(PermissionNodes.TERMS)
                        .setPlayerOnly().defaultSubcommand("list"),
                new CommandArgument("terms", true)
        );


        registerSubcommand(new SCmdTermsAccept(plugin, this));
        registerSubcommand(new SCmdTermsDecline(plugin, this));
        registerSubcommand(new SCmdTermsList(plugin, this));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        Terms terms = plugin.getModule(TermsManager.class).getTerms(args[0]);
        if (terms == null) {
            MessageReference.create(FlexCore.class, "terms.errors.not_found", new ReplacementMap("{TERMS}", args[0]).getMap()).sendTo(sender);
            return;
        }

        ListBuilder builder = new ListBuilder("subtitle", "Terms", terms.getName(), label);

        builder.addMessage(ArrayUtils.stringArrayToString(terms.getContent(), "\n"));
        builder.addMessage(MessageReference.create(FlexCore.class, "terms.usages.decide", new ReplacementMap("{LABEL}", label).put("{TERMS}", terms.getName().toLowerCase()).getMap()));

        builder.sendTo(sender, 1);
    }

}