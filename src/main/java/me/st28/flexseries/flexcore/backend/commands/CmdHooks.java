package me.st28.flexseries.flexcore.backend.commands;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.command.CommandArgument;
import me.st28.flexseries.flexcore.command.CommandUtils;
import me.st28.flexseries.flexcore.command.FlexCommand;
import me.st28.flexseries.flexcore.command.FlexCommandSettings;
import me.st28.flexseries.flexcore.hook.Hook;
import me.st28.flexseries.flexcore.hook.HookManager;
import me.st28.flexseries.flexcore.hook.HookStatus;
import me.st28.flexseries.flexcore.list.ListBuilder;
import me.st28.flexseries.flexcore.permission.PermissionNodes;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import me.st28.flexseries.flexcore.util.QuickMap;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.*;

public final class CmdHooks extends FlexCommand<FlexCore> {

    public CmdHooks(FlexCore plugin) {
        super(plugin, "flexhooks", Collections.singletonList(new CommandArgument("page", false)), new FlexCommandSettings().permission(PermissionNodes.HOOKS));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        int page = CommandUtils.getPage(args, 0);

        HookManager hookManager = FlexPlugin.getRegisteredModule(HookManager.class);

        List<Hook> hooks = new ArrayList<>(hookManager.getHooks());
        Collections.sort(hooks, new Comparator<Hook>() {
            @Override
            public int compare(Hook o1, Hook o2) {
                return o1.getPluginName().compareTo(o2.getPluginName());
            }
        });

        ListBuilder builder = new ListBuilder("page", "Hooks", null, label);

        for (Hook hook : hooks) {
            HookStatus status = hookManager.getHookStatus(hook);
            Plugin hookPlugin = hook.getPlugin();

            if (status == HookStatus.ENABLED) {
                builder.addMessage("hook_enabled", new QuickMap<>("{PLUGIN}", hookPlugin.getName()).put("{VERSION}", hookPlugin.getDescription().getVersion()).getMap());
            } else {
                String statusString;
                switch (status) {
                    case DISABLED_CONFIG:
                        statusString = "disabled";
                        break;

                    case DISABLED_DEPENDENCY:
                        statusString = "plugin not loaded";
                        break;

                    default:
                        statusString = "error";
                        break;
                }
                builder.addMessage("hook_disabled", new QuickMap<>("{PLUGIN}", hook.getPluginName()).put("{STATUS}", statusString).getMap());
            }
        }

        builder.sendTo(sender, page);
    }

}