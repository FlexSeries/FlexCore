package me.st28.flexseries.flexcore.commands;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.messages.ReplacementMap;
import me.st28.flexseries.flexcore.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.utils.PluginUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class CmdSave extends FlexCommand<FlexCore> {

    public CmdSave(FlexCore plugin) {
        super(plugin, new String[]{ "flexsave" }, null, new FlexCommandSettings<FlexCore>().description("Universal save command for FlexPlugins").permission(PermissionNodes.SAVE), new CommandArgument("plugin", true));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args) {
        String properName = PluginUtils.getProperPluginName(args[0]);
        if (properName == null) {
            MessageReference.create(FlexCore.class, "general.errors.plugin_not_found", new ReplacementMap("{NAME}", args[0]).getMap()).sendTo(sender);
            return;
        }

        Plugin targetPlugin = Bukkit.getPluginManager().getPlugin(properName);
        if (!(targetPlugin instanceof FlexPlugin)) {
            MessageReference.create(FlexCore.class, "lib_plugin.errors.plugin_not_astplugin", new ReplacementMap("{NAME}", properName).getMap()).sendTo(sender);
            return;
        }

        ((FlexPlugin) targetPlugin).saveAll(true);
        MessageReference.createGeneral((JavaPlugin) targetPlugin, "lib_plugin.notices.plugin_saved").sendTo(sender);
    }

}