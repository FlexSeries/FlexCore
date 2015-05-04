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
package me.st28.flexseries.flexcore.plugin;

import me.st28.flexseries.flexcore.events.PluginReloadedEvent;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.message.MessageManager;
import me.st28.flexseries.flexcore.plugin.exceptions.ModuleDisabledException;
import me.st28.flexseries.flexcore.plugin.module.FlexModule;
import me.st28.flexseries.flexcore.plugin.module.ModuleStatus;
import me.st28.flexseries.flexcore.util.TimeUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Represents a Bukkit plugin that uses FlexCore's plugin framework.
 */
public abstract class FlexPlugin extends JavaPlugin {

    /**
     * References to each FlexPlugin's autosaving runnable (where applicable).
     */
    private static final Map<Class<? extends FlexPlugin>, BukkitRunnable> AUTOSAVE_RUNNABLES = new HashMap<>();

    /**
     * An index of each FlexPlugin's registered modules, for easy reference via {@link FlexPlugin#getRegisteredModule(Class)}.
     */
    private static final Map<Class<? extends FlexModule>, FlexPlugin> REGISTERED_MODULES = new HashMap<>();

    /**
     * Retrieves a registered {@link FlexModule} from any loaded {@link FlexPlugin}.
     *
     * @param clazz The class of the module.
     * @return The module matching the class.
     * @throws java.lang.IllegalArgumentException Thrown if there is no registered module matching the given class.
     */
    public static <T extends FlexModule> T getRegisteredModule(Class<T> clazz) {
        Validate.notNull(clazz, "Module class cannot be null.");

        if (!REGISTERED_MODULES.containsKey(clazz)) {
            throw new IllegalArgumentException("No module with class '" + clazz.getCanonicalName() + "' is registered.");
        }

        FlexPlugin plugin = REGISTERED_MODULES.get(clazz);
        if (plugin.getModuleStatus(clazz) != ModuleStatus.ENABLED) {
            throw new ModuleDisabledException(plugin.getModule(clazz));
        }

        return plugin.getModule(clazz);
    }

    /**
     * Retrieves a registered {@link FlexModule} from any loaded {@link FlexPlugin} but returns null if an error occurred.
     * @see #getRegisteredModule(Class)
     */
    public static <T extends FlexModule> T getRegisteredModuleSilent(Class<T> clazz) {
        Validate.notNull(clazz, "Module class cannot be null.");

        if (!REGISTERED_MODULES.containsKey(clazz)) {
            return null;
        }

        FlexPlugin plugin = REGISTERED_MODULES.get(clazz);
        if (plugin.getModuleStatus(clazz) != ModuleStatus.ENABLED) {
            return null;
        }

        return plugin.getModule(clazz);
    }

    /**
     * The current status of the plugin.
     */
    private PluginStatus status;

    /**
     * Whether or not the plugin has a configuration file.
     */
    private boolean hasConfig;

    /**
     * The {@link FlexModule}s registered under this plugin.
     */
    private final Map<Class<? extends FlexModule>, FlexModule> modules = new HashMap<>();

    /**
     * An index of the module identifiers.
     */
    private final Map<String, FlexModule> moduleIdentifiers = new HashMap<>();

    /**
     * The current load statuses of each registered {@link FlexModule}.
     */
    private final Map<Class<? extends FlexModule>, ModuleStatus> moduleStatuses = new HashMap<>();

    @Override
    public final void onLoad() {
        // Mark plugin as loading and start loading data.
        status = PluginStatus.LOADING;

        try {
            handlePluginLoad();
        } catch (Exception ex) {
            LogHelper.severe(this, "An error occurred while loading: " + ex.getMessage());
            LogHelper.severe(this, "The plugin will be disabled to help prevent any damage from occurring.");
            ex.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public final void onEnable() {
        // Mark plugin as enabling and start enabling registered modules.
        status = PluginStatus.ENABLING;

        long loadStartTime = System.currentTimeMillis();

        // Register self as a listener if implemented.
        if (this instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) this, this);
        }

        // Determine if the plugin has a configuration file or not, and save it if there is one.
        if (getResource("config.yml") != null) {
            saveDefaultConfig();
            getConfig().options().copyDefaults(true);
            saveConfig();

            hasConfig = true;
            reloadConfig();
        }

        // Load modules
        //TODO: Detect circular dependencies
        List<String> disabledDependencies = hasConfig ? getConfig().getStringList("disabled modules") : null;
        List<Class<? extends FlexModule>> loadOrder = new ArrayList<>();

        for (FlexModule module : modules.values()) {
            addToLoadOrder(loadOrder, module);
        }

        // !!DEBUG!! //
        StringBuilder loadOrderDebug = new StringBuilder("\n--MODULE LOAD ORDER--\n");
        for (Class<? extends FlexModule> clazz : loadOrder) {
            if (loadOrderDebug.length() > 0) {
                loadOrderDebug.append("\n");
            }
            loadOrderDebug.append(clazz.getCanonicalName());
        }
        loadOrderDebug.append("\n\n--MODULE LOAD ORDER--\n");

        LogHelper.debug(this, loadOrderDebug.toString());
        // !!DEBUG!! //

        // This monstrosity of a loop determines the order in which modules should be loaded and loads them in the appropriate order.
        _moduleLoop: // EWWWWWWWWWW. Yeah, I know.
        for (Class<? extends FlexModule> clazz : loadOrder) {
            FlexModule<?> module = modules.get(clazz);
            LogHelper.info(this, "Loading module: " + module.getIdentifier());

            // Check if the module is disabled via the configuration file.
            if (disabledDependencies != null && disabledDependencies.contains(module.getIdentifier())) {
                moduleStatuses.put(clazz, ModuleStatus.DISABLED_CONFIG);
                LogHelper.info(this, "Module '" + module.getIdentifier() + "' disabled via the config.");
                continue;
            }

            // Locate dependencies to make sure all are present.
            for (Class<? extends FlexModule> dep : module.getDependencies()) {
                // Checks if the dependency is present on the server.
                if (!REGISTERED_MODULES.containsKey(dep)) {
                    moduleStatuses.put(clazz, ModuleStatus.DISABLED_DEPENDENCY);
                    LogHelper.severe(this, "Unable to load module '" + module.getIdentifier() + "': dependency '" + dep.getCanonicalName() + "' is not a valid module on the server.");
                    continue _moduleLoop;
                }

                // Checks if the dependency is enabled.
                if (REGISTERED_MODULES.get(dep).getModuleStatus(dep) != ModuleStatus.ENABLED) {
                    moduleStatuses.put(clazz, ModuleStatus.DISABLED_DEPENDENCY);
                    LogHelper.severe(this, "Unable to load module '" + module.getIdentifier() + "': dependency '" + dep.getCanonicalName() + "' is disabled.");
                    continue _moduleLoop;
                }
            }

            // Attempts to load the module.
            try {
                module.loadAll();

                moduleStatuses.put(clazz, ModuleStatus.ENABLED);
                LogHelper.info(this, "Successfully loaded module: " + module.getIdentifier());
            } catch (Exception ex) {
                moduleStatuses.put(clazz, ModuleStatus.DISABLED_ERROR);
                LogHelper.severe(this, "An error occurred while loading module '" + module.getIdentifier() + "': " + ex.getMessage());
                ex.printStackTrace();
            }

            if (moduleStatuses.get(clazz) != ModuleStatus.ENABLED) {
                modules.remove(clazz);
            }
        }

        // If the plugin has a messages.yml file, a MessageProvider will be created for it.
        if (getResource("messages.yml") != null) {
            MessageManager.registerMessageProvider(this);
        }

        // Attempts to enable the plugin.
        try {
            handlePluginEnable();
            handlePluginReload();
        } catch (Exception ex) {
            LogHelper.severe(this, "An error occurred while enabling: " + ex.getMessage());
            status = PluginStatus.LOADED_ERROR;
            ex.printStackTrace();
            return;
        }

        status = PluginStatus.ENABLED;
        LogHelper.info(this, String.format("%s v%s by %s ENABLED (%dms)", getName(), getDescription().getVersion(), getDescription().getAuthors(), System.currentTimeMillis() - loadStartTime));
    }

    private void addToLoadOrder(List<Class<? extends FlexModule>> loadOrder, FlexModule<?> module) {
        Class<? extends FlexModule> clazz = module.getClass();
        if (loadOrder.contains(clazz)) return;

        for (Class<? extends FlexModule> depClass : module.getDependencies()) {
            if (modules.containsKey(depClass)) {
                // From this plugin

                addToLoadOrder(loadOrder, modules.get(depClass));
            }
        }

        loadOrder.add(clazz);
    }

    @Override
    public final void onDisable() {
        status = PluginStatus.DISABLING;

        try {
            saveAll(false);
        } catch (Exception ex) {
            LogHelper.warning(this, "An error occurred while saving: " + ex.getMessage());
            ex.printStackTrace();
        }

        try {
            handlePluginDisable();
        } catch (Exception ex) {
            LogHelper.warning(this, "An error occurred while disabling: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Reloads:
     * <ul>
     *     <li>Plugin configuration file</li>
     *     <li>Registered {@link FlexModule}s</li>
     *     <li>Custom reload tasks ({@link #handlePluginReload()}</li>
     * </ul>
     */
    public final void reloadAll() {
        reloadConfig();

        for (FlexModule module : modules.values()) {
            if (getModuleStatus(module.getClass()) == ModuleStatus.ENABLED) {
                module.reloadAll();
            }
        }

        handlePluginReload();
        Bukkit.getPluginManager().callEvent(new PluginReloadedEvent(this.getClass()));
    }

    @Override
    public final void reloadConfig() {
        super.reloadConfig();
        if (hasConfig) {
            int autosaveInterval = getConfig().getInt("autosave interval", 0);
            if (autosaveInterval == 0) {
                LogHelper.warning(this, "Autosaving disabled. It is recommended to enable it to help prevent data loss!");
            } else {
                if (AUTOSAVE_RUNNABLES.containsKey(getClass())) {
                    AUTOSAVE_RUNNABLES.remove(getClass()).cancel();
                }

                BukkitRunnable runnable = new BukkitRunnable() {
                    @Override
                    public void run() {
                        saveAll(true);
                    }
                };

                runnable.runTaskTimer(this, autosaveInterval * 1200L, autosaveInterval * 1200L);
                AUTOSAVE_RUNNABLES.put(getClass(), runnable);
                LogHelper.info(this, "Autosaving enabled. Saving every " + TimeUtils.translateSeconds(autosaveInterval * 60) + ".");
            }

            handleConfigReload(getConfig());
        }
    }

    /**
     * Saves the entirety of the plugin.
     *
     * @param async If true, should save asynchronously (where applicable).
     */
    public final void saveAll(boolean async) {
        if (hasConfig) {
            saveConfig();
        }

        for (FlexModule<?> module : modules.values()) {
            if (getModuleStatus(module.getClass()) == ModuleStatus.ENABLED) {
                module.saveAll(async);
            }
        }

        handlePluginSave(async);
    }

    /**
     * @return true if the plugin has a configuration file.
     */
    public final boolean hasConfig() {
        return hasConfig;
    }

    /**
     * @return the current status of the plugin.
     */
    public final PluginStatus getStatus() {
        return status;
    }

    /**
     * @return the current status for a module registered for this plugin.
     * @throws java.lang.IllegalArgumentException Thrown if the module is not registered under this plugin.
     */
    public final ModuleStatus getModuleStatus(Class<? extends FlexModule> module) {
        if (!moduleStatuses.containsKey(module)) {
            throw new IllegalArgumentException("Module with class '" + module.getCanonicalName() + "' is not registered underneath this plugin.");
        }
        return moduleStatuses.get(module);
    }

    /**
     * @return a read-only collection of all of the registered modules for this plugin.
     */
    public final Collection<FlexModule> getModules() {
        return Collections.unmodifiableCollection(modules.values());
    }

    /**
     * @return a module registered for this plugin.
     * @throws java.lang.IllegalArgumentException Thrown if the module is not registered under this plugin.
     */
    public final <T extends FlexModule> T getModule(Class<T> clazz) {
        if (!modules.containsKey(clazz)) {
            throw new IllegalArgumentException("Module with class '" + clazz.getCanonicalName() + "' is not registered underneath this plugin.");
        }
        return (T) modules.get(clazz);
    }

    /**
     * Registers a FlexModule for this plugin.
     *
     * @param module The module to register.
     * @return True if successfully registered.<br />
     *         False if the module's class has already been registered.
     * @throws java.lang.IllegalStateException Thrown if this method after the plugin has gone through the loading phase.
     */
    public final boolean registerModule(FlexModule module) {
        Validate.notNull(module, "Module cannot be null.");
        Class<? extends FlexModule> clazz = module.getClass();
        if (REGISTERED_MODULES.containsKey(clazz)) {
            return false;
        }

        if (status != PluginStatus.LOADING) {
            throw new IllegalStateException("Currently not accepting new module registrations.");
        }

        REGISTERED_MODULES.put(clazz, this);
        modules.put(clazz, module);
        moduleStatuses.put(clazz, null);
        return true;
    }

    /**
     * Handles custom load tasks, such as, but not limited to:
     * <ul>
     *     <li>Registering modules</li>
     * </ul>
     */
    public void handlePluginLoad() {}

    /**
     * Handles custom enable tasks.
     */
    public void handlePluginEnable() {}

    /**
     * Handles custom disable tasks.
     */
    public void handlePluginDisable() {}

    /**
     * Handles custom reload tasks.
     */
    public void handlePluginReload() {}

    /**
     * Handles custom config reload tasks.
     */
    public void handleConfigReload(FileConfiguration config) {}

    /**
     * Handles custom save tasks.
     *
     * @param async If true, should save asynchronously (where applicable).
     */
    public void handlePluginSave(boolean async) {}

}