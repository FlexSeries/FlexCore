package me.st28.flexseries.flexcore.backend.commands.debug;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.command.CommandArgument;
import me.st28.flexseries.flexcore.command.CommandUtils;
import me.st28.flexseries.flexcore.command.FlexCommandSettings;
import me.st28.flexseries.flexcore.command.FlexSubcommand;
import me.st28.flexseries.flexcore.command.exceptions.CommandInterruptedException;
import me.st28.flexseries.flexcore.debug.DebugManager;
import me.st28.flexseries.flexcore.debug.DebugTest;
import me.st28.flexseries.flexcore.list.ListBuilder;
import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import me.st28.flexseries.flexcore.util.PluginUtils;
import me.st28.flexseries.flexcore.util.QuickMap;
import me.st28.flexseries.flexcore.util.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class SCmdDebugList extends FlexSubcommand<FlexCore> {

    public SCmdDebugList(CmdDebug cmdDebug) {
        super(cmdDebug, "list", Arrays.asList(new CommandArgument("plugin", false), new CommandArgument("page", false)), new FlexCommandSettings<FlexCore>().description("Lists debug tests"));
    }

    @Override
    public void runCommand(CommandSender sender, String command, String label, String[] args, Map<String, String> parameters) {
        boolean pageArgFirst = true;

        int page = CommandUtils.getPage(args, 0, true);
        if (page == -1) {
            pageArgFirst = false;
            page = CommandUtils.getPage(args, 1);
        }

        Map<Class<? extends JavaPlugin>, Map<String, DebugTest>> debugTests = FlexPlugin.getRegisteredModule(DebugManager.class).getDebugTests();

        if (pageArgFirst) {
            ListBuilder builder = new ListBuilder("title", "Debug Tests", null, label);

            if (!debugTests.isEmpty()) {
                List<String> pluginNames = new ArrayList<>();
                for (Class<? extends JavaPlugin> pluginClass : debugTests.keySet()) {
                    pluginNames.add(JavaPlugin.getPlugin(pluginClass).getName());
                }

                builder.addMessage(StringUtils.stringCollectionToString(pluginNames, ", "));
            }

            builder.sendTo(sender, 1);
        } else {
            JavaPlugin plugin = (JavaPlugin) PluginUtils.getPlugin(args[0]);
            if (plugin == null) {
                throw new CommandInterruptedException(MessageReference.createGeneral(FlexCore.class, "errors.plugin_not_found", new QuickMap<>("{NAME}", args[0]).getMap()));
            }

            Map<String, DebugTest> tests = debugTests.get(plugin.getClass());

            ListBuilder builder = new ListBuilder("page_subtitle", "Debug Tests", plugin.getName(), label);

            if (tests != null) {
                for (Entry<String, DebugTest> entry : tests.entrySet()) {
                    builder.addMessage("title", entry.getKey(), entry.getValue().getDescription());
                }
            }

            builder.sendTo(sender, page);
        }
    }

}