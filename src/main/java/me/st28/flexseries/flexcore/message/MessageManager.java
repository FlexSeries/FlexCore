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

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.plugin.module.FlexModule;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class MessageManager extends FlexModule<FlexCore> {

    private static MessageManager getInstance() {
        return FlexPlugin.getRegisteredModule(MessageManager.class);
    }

    public static MessageProvider getMessageProvider(Class<? extends JavaPlugin> plugin) {
        MessageManager instance = getInstance();
        return !instance.messageProviders.containsKey(plugin) ? instance.invalidMessageProvider : instance.messageProviders.get(plugin);
    }

    public static String getGeneralMessage(JavaPlugin plugin, String path) {
        return getMessageProvider(FlexCore.class).getMessage(plugin, path);
    }

    /*public static String getGeneralMessage(JavaPlugin plugin, String path, Map<String, Object> replacements) {
        return getMessageProvider(FlexCore.class).getMessage(plugin, "general." + path, replacements);
    }*/

    /**
     * Registers a {@link MessageProvider} for a plugin.
     *
     * @param plugin The plugin to register.
     * @return The newly created or existing {@link MessageProvider} for the plugin.
     * @throws IllegalArgumentException Thrown if the plugin does not contain a messages.yml file within the jar.
     */
    public static MessageProvider registerMessageProvider(JavaPlugin plugin) {
        if (plugin.getResource("messages.yml") == null) {
            throw new IllegalArgumentException("Plugin '" + plugin.getName() + "' does not have a default messages.yml file in the jar.");
        }

        MessageManager instance = getInstance();
        Class<? extends JavaPlugin> clazz = plugin.getClass();

        if (instance.messageProviders.containsKey(clazz)) {
            return instance.messageProviders.get(clazz);
        } else {
            MessageProvider provider = new MessageProvider(plugin);
            instance.messageProviders.put(clazz, provider);
            instance.reloadMessageProvider(provider);
            return provider;
        }
    }

    private InvalidMessageProvider invalidMessageProvider;
    private Map<Class<? extends JavaPlugin>, MessageProvider> messageProviders = new HashMap<>();

    public MessageManager(FlexCore plugin) {
        super(plugin, "messages", "Handles customizable messages for plugins.", false);

        isDisableable = false;

        invalidMessageProvider = new InvalidMessageProvider();
    }

    @Override
    public void handleReload() {
        for (MessageProvider provider : messageProviders.values()) {
            reloadMessageProvider(provider);
        }
    }

    private void reloadMessageProvider(MessageProvider provider) {
        try {
            provider.reload();
        } catch (IOException ex) {
            LogHelper.warning(this, "An exception occurred while loading the MessageProvider for: " + provider.plugin.getName());
            ex.printStackTrace();
        }
    }

}