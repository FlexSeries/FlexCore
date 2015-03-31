package me.st28.flexseries.flexcore.backend.commands;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.command.CommandArgument;
import me.st28.flexseries.flexcore.command.FlexCommand;
import me.st28.flexseries.flexcore.command.FlexCommandSettings;
import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.message.ReplacementMap;
import me.st28.flexseries.flexcore.permission.PermissionNodes;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import me.st28.flexseries.flexcore.util.PluginUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Map;

public final class CmdReload extends FlexCommand<FlexCore> {

    public CmdReload(FlexCore plugin) {
        super(plugin, "flexreload", Collections.singletonList(new CommandArgument("plugin", true)), new FlexCommandSettings().permission(PermissionNodes.RELOAD));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
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