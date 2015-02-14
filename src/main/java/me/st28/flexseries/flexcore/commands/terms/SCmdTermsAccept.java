package me.st28.flexseries.flexcore.commands.terms;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.commands.CommandUtils;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.messages.ReplacementMap;
import me.st28.flexseries.flexcore.terms.Terms;
import me.st28.flexseries.flexcore.terms.TermsManager;
import org.bukkit.command.CommandSender;

public final class SCmdTermsAccept extends FlexCommand<FlexCore> {

    public SCmdTermsAccept(FlexCore plugin, FlexCommand<FlexCore> parent) {
        super(
                plugin,
                new String[]{"accept"},
                parent,
                new FlexCommandSettings<FlexCore>()
                        .description("Accept Terms of Use")
                        .setInheritPermission(),
                new CommandArgument("terms", true)
        );
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args) {
        Terms terms = plugin.getModule(TermsManager.class).getTerms(args[0]);
        if (terms == null) {
            MessageReference.create(FlexCore.class, "terms.errors.not_found", new ReplacementMap("{TERMS}", args[0]).getMap()).sendTo(sender);
            return;
        }

        if (!plugin.getModule(TermsManager.class).setAgreement(CommandUtils.getSenderUuid(sender), terms, true)) {
            MessageReference.create(FlexCore.class, "terms.errors.already_accepted", new ReplacementMap("{TERMS}", terms.getName()).getMap()).sendTo(sender);
        } else {
            MessageReference.create(FlexCore.class, "terms.notices.accepted", new ReplacementMap("{TERMS}", terms.getName()).getMap()).sendTo(sender);
        }
    }

}