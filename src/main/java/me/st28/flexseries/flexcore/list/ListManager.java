package me.st28.flexseries.flexcore.list;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.plugin.module.FlexModule;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper for commands that have list-based output with a header.<br />
 * Allows multiple plugins to have consistently styled lists.
 * <br />
 * <b>Example:</b><br />
 * ===== Topic (pg.1/5) =====<br />
 * 1) Element 1<br />
 * 2) Element 2
 */
public final class ListManager extends FlexModule<FlexCore> {

    static ListManager getInstance() {
        return FlexPlugin.getRegisteredModule(ListManager.class);
    }

    int pageItems;
    int lineLength = 54;

    String msgNextPage;
    String msgNoElements;

    private final String UNKNOWN_ELEMENT_FORMAT = "&cUnknown element format: &6{NAME}&c.";
    private Map<String, String> formatsElement = new HashMap<>();
    private Map<String, ListHeader> formatsHeader = new HashMap<>();

    public ListManager(FlexCore plugin) {
        super(plugin, "lists", "Handles the creation and formatting of lists that are displayed as output for commands", false);
    }

    @Override
    protected void handleReload() {
        FileConfiguration config = getConfig();

        pageItems = config.getInt("General Config.page items", 8);
        lineLength = config.getInt("General Config.line length", 54);

        msgNextPage = config.getString("Messages.next page", "&c&oType &6&o/{COMMAND} {NEXTPAGE} &c&oto view the next page.");
        msgNoElements = config.getString("Messages.no elements", "&c&oNothing here.");

        formatsElement.clear();
        ConfigurationSection elementSec = config.getConfigurationSection("Formats.Elements");
        if (elementSec != null) {
            for (String key : elementSec.getKeys(false)) {
                if (formatsElement.containsKey(key.toLowerCase())) {
                    LogHelper.warning(FlexCore.class, "A element format named '" + key.toLowerCase() + "' is already loaded.");
                    continue;
                }

                formatsElement.put(key.toLowerCase(), elementSec.getString(key));
            }
        }

        formatsHeader.clear();
        ConfigurationSection headerSec = config.getConfigurationSection("Formats.Headers");
        if (headerSec != null) {
            for (String key : headerSec.getKeys(false)) {
                if (formatsHeader.containsKey(key.toLowerCase())) {
                    LogHelper.warning(FlexCore.class, "A header format named '" + key.toLowerCase() + "' is already loaded.");
                    continue;
                }

                ListHeader header;
                try {
                    header = new ListHeader(headerSec.getConfigurationSection(key));
                } catch (Exception ex) {
                    LogHelper.warning(FlexCore.class, "An exception occurred while loading header '" + key.toLowerCase() + "'");
                    ex.printStackTrace();
                    continue;
                }
                formatsHeader.put(key.toLowerCase(), header);
            }
        }
    }

    public String getElementFormat(String name) {
        name = name.toLowerCase();
        return !formatsElement.containsKey(name) ? UNKNOWN_ELEMENT_FORMAT.replace("{NAME}", name) : formatsElement.get(name);
    }

    public ListHeader getHeaderFormat(String name) {
        name = name.toLowerCase();
        return !formatsHeader.containsKey(name) ? new ListHeader(name) : formatsHeader.get(name);
    }

}