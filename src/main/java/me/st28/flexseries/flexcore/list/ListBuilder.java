/**
 * FlexCore - Licensed under the MIT License (MIT)
 *
 * Copyright (c) Stealth2800 <http://stealthyone.com/>
 * Copyright (c) contributors <https://github.com/FlexSeries>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.st28.flexseries.flexcore.list;

import com.stealthyone.mcb.mcml.MCMLBuilder;
import com.stealthyone.mcb.mcml.shade.fanciful.FancyMessage;
import me.st28.flexseries.flexcore.message.MessageReference;
import me.st28.flexseries.flexcore.message.ReplacementMap;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import me.st28.flexseries.flexcore.util.MiscUtils;
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
        returnList.add(new MCMLBuilder(header.getFormattedHeader(page, maxPages, headerKey, headerValue)).getFancyMessage());
        for (int i = 0; i < pageItems; i++) {
            int curIndex = i + ((page - 1) * pageItems);

            try {
                returnList.add(messages.get(curIndex).getMessage(new ReplacementMap("{LABEL}", label).put("{INDEX}", Integer.toString(curIndex + 1)).getMap()));
            } catch (Exception ex) {
                if (i == 0) {
                    returnList.add(new MCMLBuilder(listManager.msgNoElements).getFancyMessage());
                }
                break;
            }

            if (i == pageItems - 1 && page < maxPages && nextPageCommand != null) {
                returnList.add(new MCMLBuilder(listManager.msgNextPage.replace("{COMMAND}", nextPageCommand.replace("{LABEL}", label)).replace("{NEXTPAGE}", Integer.toString(page + 1))).getFancyMessage());
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