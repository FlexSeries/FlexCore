package me.st28.flexseries.flexcore.backend.commands;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.commands.CommandArgument;
import me.st28.flexseries.flexcore.commands.CommandUtils;
import me.st28.flexseries.flexcore.commands.FlexCommand;
import me.st28.flexseries.flexcore.commands.FlexCommandSettings;
import me.st28.flexseries.flexcore.lists.ListBuilder;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.messages.ReplacementMap;
import me.st28.flexseries.flexcore.permissions.PermissionNodes;
import me.st28.flexseries.flexcore.plugins.FlexModule;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.utils.PluginUtils;
import me.st28.flexseries.flexcore.utils.QuickMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.*;

public final class CmdModules extends FlexCommand<FlexCore> {

    public CmdModules(FlexCore plugin) {
        super(plugin, new String[]{"flexmodules", "modules"}, null, new FlexCommandSettings<FlexCore>().description("View information about registered modules under loaded FlexPlugins").permission(PermissionNodes.MODULES), new CommandArgument("plugin", true), new CommandArgument("page", false));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        int page = CommandUtils.getPage(args, 1);

        String name = PluginUtils.getProperPluginName(args[0]);

        if (name == null) {
            MessageReference.create(FlexCore.class, "general.errors.plugin_not_found", new ReplacementMap("{NAME}", args[0]).getMap()).sendTo(sender);
            return;
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        if (!(plugin instanceof FlexPlugin)) {
            MessageReference.create(FlexCore.class, "lib_plugin.errors.plugin_not_flexplugin", new ReplacementMap("{NAME}", name).getMap()).sendTo(sender);
            return;
        }

        List<FlexModule> modules = new ArrayList<>(((FlexPlugin) plugin).getModules());
        Collections.sort(modules, new Comparator<FlexModule>() {
            @Override
            public int compare(FlexModule o1, FlexModule o2) {
                return o1.getIdentifier().toLowerCase().compareTo(o2.getIdentifier().toLowerCase());
            }
        });

        ListBuilder builder = new ListBuilder("page_subtitle", "Modules", name, label);

        for (FlexModule module : modules) {
            String status;
            switch (module.getStatus()) {
                case ENABLED:
                    status = ChatColor.GREEN + "enabled";
                    break;

                case DISABLED_CONFIG:
                    status = ChatColor.DARK_RED + "disabled";
                    break;

                case DISABLED_DEPENDENCY:
                    status = ChatColor.RED + "missing dependency";
                    break;

                default:
                    status = ChatColor.RED + "error";
                    break;
            }

            builder.addMessage("lib_plugin_module", new QuickMap<>("{STATUS}", status).put("{NAME}", module.getIdentifier()).put("{DESCRIPTION}", module.getDescription()).getMap());
        }

        //TODO: Don't include page number if one was specified
        builder.enableNextPageNotice(command.replace("/", ""));
        builder.sendTo(sender, page);
    }

}