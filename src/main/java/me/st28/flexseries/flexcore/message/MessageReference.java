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
package me.st28.flexseries.flexcore.message;

import com.stealthyone.mcb.mcml.MCMLBuilder;
import com.stealthyone.mcb.mcml.shade.fanciful.FancyMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//TODO: Allow for multi-message message references
public final class MessageReference {

    public static MessageReference combine(MessageReference initial, String replacementKey, MessageReference replacement) {
        Map<String, String> newReplacements = new HashMap<>();
        newReplacements.putAll(initial.replacements);
        newReplacements.putAll(replacement.replacements);

        Map<String, Object> newItemReplacements = new HashMap<>();
        newItemReplacements.putAll(initial.itemReplacements);
        newItemReplacements.putAll(replacement.itemReplacements);

        return new MessageReference(initial.rawMessage.replace(replacementKey, replacement.rawMessage), newReplacements, newItemReplacements);
    }

    public static MessageReference createPlain(String message) {
        return new MessageReference(message);
    }

    public static MessageReference create(Class<? extends JavaPlugin> plugin, String identifier) {
        return create(plugin, identifier, null);
    }

    public static MessageReference create(Class<? extends JavaPlugin> plugin, String identifier, Map<String, String> replacements) {
        if (replacements == null || replacements.isEmpty()) {
            return new MessageReference(MessageManager.getMessageProvider(plugin).getMessage(identifier));
        } else {
            return new MessageReference(MessageManager.getMessageProvider(plugin).getMessage(identifier), replacements);
        }
    }

    public static MessageReference createGeneral(Class<? extends JavaPlugin> plugin, String identifier) {
        return createGeneral(JavaPlugin.getPlugin(plugin), identifier, null);
    }

    public static MessageReference createGeneral(Class<? extends JavaPlugin> plugin, String identifier, Map<String, String> replacements) {
        return createGeneral(JavaPlugin.getPlugin(plugin), identifier, replacements);
    }

    public static MessageReference createGeneral(JavaPlugin plugin, String identifier) {
        return createGeneral(plugin, identifier, null);
    }

    public static MessageReference createGeneral(JavaPlugin plugin, String identifier, Map<String, String> replacements) {
        if (replacements == null || replacements.isEmpty()) {
            return new MessageReference(MessageManager.getGeneralMessage(plugin, identifier));
        } else {
            return new MessageReference(MessageManager.getGeneralMessage(plugin, identifier), replacements);
        }
    }

    private final String rawMessage;
    private final Map<String, String> replacements = new HashMap<>();
    private final Map<String, Object> itemReplacements = new HashMap<>();

    private MessageReference(String message) {
        this.rawMessage = message;
    }

    private MessageReference(String message, Map<String, String> replacements) {
        this.rawMessage = message;
        this.replacements.putAll(replacements);
    }

    private MessageReference(String message, Map<String, String> replacements, Map<String, Object> itemReplacements) {
        this.rawMessage = message;
        this.replacements.putAll(replacements);
        this.itemReplacements.putAll(itemReplacements);
    }

    public MessageReference addItemReplacement(String key, String itemJson) {
        itemReplacements.put(key, itemJson);
        return this;
    }

    public MessageReference addItemReplacement(String key, ItemStack item) {
        itemReplacements.put(key, item);
        return this;
    }

    public String getPlainMessage() {
        String returnMessage = new MCMLBuilder(rawMessage, replacements).buildFancyMessage().toOldMessageFormat();
        if (!rawMessage.startsWith(ChatColor.WHITE.toString()) && returnMessage.startsWith(ChatColor.WHITE.toString())) {
            return returnMessage.substring(2);
        } else {
            return returnMessage;
        }
    }

    public FancyMessage getMessage() {
        return getMessage(null);
    }

    public FancyMessage getMessage(Map<String, String> additionalReplacements) {
        Map<String, String> newReplacements = new HashMap<>();

        if (replacements != null) {
            newReplacements.putAll(replacements);
        }

        if (additionalReplacements != null) {
            newReplacements.putAll(additionalReplacements);
        }

        return new MCMLBuilder(rawMessage, newReplacements, itemReplacements).buildFancyMessage();
    }

    public void sendTo(CommandSender... recipients) {
        sendTo(Arrays.asList(recipients), null);
    }

    public void sendTo(Collection<CommandSender> recipients, Map<String, String> additionalReplacements) {
        if (rawMessage == null) return;
        getMessage(additionalReplacements).send(recipients);
    }

}