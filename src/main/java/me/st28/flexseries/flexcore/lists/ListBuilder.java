package me.st28.flexseries.flexcore.lists;

import com.stealthyone.mcb.mcml.MCMLBuilder;
import com.stealthyone.mcb.mcml.shade.fanciful.FancyMessage;
import me.st28.flexseries.flexcore.messages.MessageReference;
import me.st28.flexseries.flexcore.messages.ReplacementMap;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.utils.MiscUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class ListBuilder {

    private static ListManager getListManager() {
        return FlexPlugin.getRegisteredModule(ListManager.class);
    }

    private ListHeader header;
    private String headerKey;
    private String headerValue;

    private String nextPageCommand;

    private String label;
    private List<MessageReference> messages = new ArrayList<>();

    public ListBuilder(String header, String key, String value, String label) {
        this.header = getListManager().getHeaderFormat(header);
        this.headerKey = key;
        this.headerValue = value;
        this.label = label;
    }

    public void addMessage(MessageReference reference) {
        messages.add(reference);
    }

    public void addMessage(String message) {
        messages.add(MessageReference.createPlain(message));
    }

    public void addMessage(String format, String key, String value) {
        String rawFormat = getListManager().getElementFormat(format).replace("{KEY}", key);
        if (value != null) {
            rawFormat = rawFormat.replace("{VALUE}", value);
        }
        messages.add(MessageReference.createPlain(rawFormat));
    }

    public void addMessage(String format, Map<String, String> replacements) {
        String rawFormat = getListManager().getElementFormat(format);
        for (Entry<String, String> entry : replacements.entrySet()) {
            rawFormat = rawFormat.replace(entry.getKey(), entry.getValue());
        }
        messages.add(MessageReference.createPlain(rawFormat));
    }

    public void enableNextPageNotice(String nextPageCommand) {
        this.nextPageCommand = nextPageCommand;
    }

    public int getPageItems() {
        return getListManager().pageItems;
    }

    public List<FancyMessage> getMessages(int page) {
        ListManager listManager = getListManager();

        int pageItems = getPageItems();
        int maxPages = MiscUtils.getPageCount(messages.size(), pageItems);

        List<FancyMessage> returnList = new ArrayList<>();
        returnList.add(new MCMLBuilder(header.getFormattedHeader(page, maxPages, headerKey, headerValue)).buildFancyMessage());
        for (int i = 0; i < pageItems; i++) {
            int curIndex = i + ((page - 1) * pageItems);

            try {
                returnList.add(messages.get(curIndex).getMessage(new ReplacementMap("{LABEL}", label).put("{INDEX}", Integer.toString(curIndex + 1)).getMap()));
            } catch (Exception ex) {
                if (i == 0) {
                    returnList.add(new MCMLBuilder(listManager.msgNoElements).buildFancyMessage());
                }
                break;
            }

            if (i == pageItems - 1 && page < maxPages && nextPageCommand != null) {
                returnList.add(new MCMLBuilder(listManager.msgNextPage.replace("{COMMAND}", nextPageCommand.replace("{LABEL}", label)).replace("{NEXTPAGE}", Integer.toString(page + 1))).buildFancyMessage());
            }
        }

        return returnList;
    }

    public void sendTo(CommandSender sender, int page) {
        boolean isPlayer = sender instanceof Player;

        for (FancyMessage message : getMessages(page)) {
            if (!isPlayer) {
                sender.sendMessage(message.toOldMessageFormat());
            } else {
                message.send(sender);
            }
        }
    }

}