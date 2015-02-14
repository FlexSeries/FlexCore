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

public final class CmdReload extends FlexCommand<FlexCore> {

    public CmdReload(FlexCore plugin) {
        super(plugin, new String[]{"flexreload"}, null, new FlexCommandSettings<FlexCore>().description("Universal reload command for FlexPlugins").permission(PermissionNodes.RELOAD), new CommandArgument("plugin", true));
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
            MessageReference.create(FlexCore.class, "lib_astplugin.errors.plugin_not_astplugin", new ReplacementMap("{NAME}", args[0]).getMap()).sendTo(sender);
            return;
        }

        ((FlexPlugin) targetPlugin).reloadAll();
        MessageReference.createGeneral((JavaPlugin) targetPlugin, "lib_plugin.notices.plugin_reloaded").sendTo(sender);
    }

}