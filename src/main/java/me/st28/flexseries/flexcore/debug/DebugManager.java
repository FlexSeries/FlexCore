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
package me.st28.flexseries.flexcore.debug;

import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.plugin.module.FlexModule;
import me.st28.flexseries.flexcore.util.PluginUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DebugManager extends FlexModule<FlexCore> {

    private final Map<Class<? extends JavaPlugin>, Map<String, DebugTest>> debugTests = new HashMap<>();

    public DebugManager(FlexCore plugin) {
        super(plugin, "debug", "Manages debug tests", false);
    }

    /**
     * Registers a debug test.
     *
     * @param test The test implementation.
     * @return True if successfully registered.<br />
     *         False if a test with the same name for the plugin is already registered.
     */
    public final boolean registerDebugTest(DebugTest test) {
        JavaPlugin plugin = test.plugin;
        String name = test.getName().toLowerCase();
        try {
            Integer.parseInt(name);
            throw new IllegalArgumentException("Debug test name cannot be an integer.");
        } catch (Exception ex) { }

        Map<String, DebugTest> pluginTests = debugTests.get(plugin.getClass());
        if (pluginTests == null) {
            pluginTests = new HashMap<>();
            debugTests.put(plugin.getClass(), pluginTests);
        }

        if (pluginTests.containsKey(name)) {
            LogHelper.warning(FlexCore.class, "Plugin '" + plugin.getName() + "' tried to register a debug test under the name '" + name + "' but it is already in use.");
            return false;
        }

        pluginTests.put(name, test);
        LogHelper.info(FlexCore.class, "Debug test '" + name + "' registered for plugin: " + plugin.getName());
        return true;
    }

    public final Map<Class<? extends JavaPlugin>, Map<String, DebugTest>> getDebugTests() {
        return Collections.unmodifiableMap(debugTests);
    }

    public final Collection<DebugTest> getDebugTests(String pluginName) {
        JavaPlugin plugin = (JavaPlugin) PluginUtils.getPlugin(pluginName);
        return plugin == null ? null : getDebugTests(plugin.getClass());
    }

    public final Collection<DebugTest> getDebugTests(JavaPlugin plugin) {
        Validate.notNull(plugin, "Plugin cannot be null.");
        return getDebugTests(plugin.getClass());
    }

    public final Collection<DebugTest> getDebugTests(Class<? extends JavaPlugin> pluginClass) {
        Validate.notNull(pluginClass, "Plugin class cannot be null.");
        return !debugTests.containsKey(pluginClass) ? null : Collections.unmodifiableCollection(debugTests.get(pluginClass).values());
    }

    public final DebugTest getDebugTest(String pluginName, String testName) {
        Validate.notNull(pluginName, "Plugin name cannot be null.");

        JavaPlugin plugin = (JavaPlugin) PluginUtils.getPlugin(pluginName);
        return plugin == null ? null : getDebugTest(plugin.getClass(), testName);
    }

    public final DebugTest getDebugTest(JavaPlugin plugin, String testName) {
        Validate.notNull(plugin, "Plugin cannot be null.");
        return getDebugTest(plugin.getClass(), testName);
    }

    public final DebugTest getDebugTest(Class<? extends JavaPlugin> pluginClass, String testName) {
        Validate.notNull(pluginClass, "Plugin class cannot be null.");
        Validate.notNull(testName, "Test name cannot be null.");

        testName = testName.toLowerCase();
        if (!debugTests.containsKey(pluginClass) || !debugTests.get(pluginClass).containsKey(testName)) {
            return null;
        }
        return debugTests.get(pluginClass).get(testName);
    }

}