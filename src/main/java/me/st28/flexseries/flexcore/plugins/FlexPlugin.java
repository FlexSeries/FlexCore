package me.st28.flexseries.flexcore.plugins;

import me.st28.flexseries.flexcore.events.PluginReloadedEvent;
import me.st28.flexseries.flexcore.help.HelpManager;
import me.st28.flexseries.flexcore.help.HelpTopic;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.messages.MessageManager;
import me.st28.flexseries.flexcore.plugins.exceptions.ModuleDisabledException;
import me.st28.flexseries.flexcore.utils.TimeUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public abstract class FlexPlugin extends JavaPlugin {

    private static final Map<Class<? extends FlexPlugin>, BukkitRunnable> AUTOSAVE_RUNNABLES = new HashMap<>();
    private static final Map<Class<? extends FlexModule>, FlexPlugin> REGISTERED_MODULES = new HashMap<>();

    /**
     * Retrieves a registered module instance.
     *
     * @param clazz The class of the module.
     * @return The module matching the class.
     * @throws java.lang.IllegalArgumentException Thrown if the input module isn't registered.
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

    private PluginStatus status;

    /**
     * Whether or not the plugin has a configuration file.
     */
    private boolean hasConfig;

    /**
     * The {@link me.st28.flexseries.flexcore.plugins.FlexModule}s registered under this plugin.
     */
    private final Map<Class<? extends FlexModule>, FlexModule> modules = new HashMap<>();

    /**
     * An index of the module identifiers.
     */
    private final Map<String, FlexModule> moduleIdentifiers = new HashMap<>();

    /**
     * The current load statuses of each registered {@link me.st28.flexseries.flexcore.plugins.FlexModule}.
     */
    private final Map<Class<? extends FlexModule>, ModuleStatus> moduleStatuses = new HashMap<>();

    @Override
    public final void onLoad() {
        status = PluginStatus.LOADING;

        handlePluginLoad();
    }

    @Override
    public final void onEnable() {
        status = PluginStatus.ENABLING;

        long loadStartTime = System.currentTimeMillis();

        // Register self as a listener if implemented.
        if (this instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) this, this);
        }

        // Determine if the plugin has a configuration file or not, and save it if there is one.
        if (getResource("config.yml") != null) {
            hasConfig = true;
            saveDefaultConfig();
            getConfig().options().copyDefaults(true);
            saveConfig();
        }

        List<String> disabledDependencies = hasConfig ? getConfig().getStringList("Disabled Modules") : null;

        // Load modules
        //TODO: Detect circular dependencies
        List<Class<? extends FlexModule>> loadOrder = new ArrayList<>();

        for (FlexModule module : modules.values()) {
            addToLoadOrder(loadOrder, module);
        }

        // Attempted easy way out, doesn't work consistently.
        /*
        List<Class<? extends FlexModule>> loadOrder = new ArrayList<>(modules.keySet());
        Collections.sort(loadOrder, new Comparator<Class<? extends FlexModule>>() {
            @Override
            public int compare(Class<? extends FlexModule> o1, Class<? extends FlexModule> o2) {
                if (o1.equals(o2)) {
                    return 0;
                }

                Collection dependencies = modules.get(o1).getDependencies();
                if (dependencies.isEmpty() || dependencies.contains(o2)) {
                    return -1;
                }

                return 1;
            }
        });*/

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

        _moduleLoop:
        for (Class<? extends FlexModule> clazz : loadOrder) {
            FlexModule<?> module = modules.get(clazz);
            LogHelper.info(this, "Loading module: " + module.getIdentifier());

            if (disabledDependencies != null && disabledDependencies.contains(module.getIdentifier())) {
                moduleStatuses.put(clazz, ModuleStatus.DISABLED_CONFIG);
                LogHelper.info(this, "Module '" + module.getIdentifier() + "' disabled via the config.");
                continue;
            }

            for (Class<? extends FlexModule> dep : module.getDependencies()) {
                if (!REGISTERED_MODULES.containsKey(dep)) {
                    moduleStatuses.put(clazz, ModuleStatus.DISABLED_DEPENDENCY);
                    LogHelper.severe(this, "Unable to load module '" + module.getIdentifier() + "': dependency '" + dep.getCanonicalName() + "' is not a valid module on the server.");
                    continue _moduleLoop;
                }

                if (REGISTERED_MODULES.get(dep).getModuleStatus(dep) != ModuleStatus.ENABLED) {
                    moduleStatuses.put(clazz, ModuleStatus.DISABLED_DEPENDENCY);
                    LogHelper.severe(this, "Unable to load module '" + module.getIdentifier() + "': dependency '" + dep.getCanonicalName() + "' is disabled.");
                    continue _moduleLoop;
                }
            }

            try {
                module.loadAll();

                moduleStatuses.put(clazz, ModuleStatus.ENABLED);
                LogHelper.info(this, "Successfully loaded module: " + module.getIdentifier());
            } catch (Exception ex) {
                moduleStatuses.put(clazz, ModuleStatus.DISABLED_ERROR);
                LogHelper.severe(this, "An error occurred while loading module '" + module.getIdentifier() + "': " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        if (getResource("messages.yml") != null) {
            MessageManager.registerMessageProvider(this);
        }

        try {
            handlePluginEnable();
            reloadConfig();
            handlePluginReload();
        } catch (Exception ex) {
            LogHelper.severe(this, "An error occurred while enabling: " + ex.getMessage());
            ex.printStackTrace();
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

        saveAll(false);

        handlePluginDisable();
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

    public final void reloadConfig() {
        if (hasConfig) {
            super.reloadConfig();

            int autosaveInterval = getConfig().getInt("Autosave Interval", 0);
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
        saveConfig();

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
    public void handlePluginLoad() { }

    /**
     * Handles custom enable tasks.
     */
    public void handlePluginEnable() { }

    /**
     * Handles custom disable tasks.
     */
    public void handlePluginDisable() { }

    /**
     * Handles custom reload tasks.
     */
    public void handlePluginReload() { }

    /**
     * Handles custom config reload tasks.
     */
    public void handleConfigReload(FileConfiguration config) { }

    /**
     * Handles custom save tasks.
     *
     * @param async If true, should save asynchronously (where applicable).
     */
    public void handlePluginSave(boolean async) { }

    /**
     * @return the name of the base {@link me.st28.flexseries.flexcore.help.HelpTopic}.
     */
    public String getHelpTopicName() {
        return getName().replace("AST", "");
    }

    /**
     * @return the description of the base {@link me.st28.flexseries.flexcore.help.HelpTopic}.
     */
    public String getHelpTopicDescription() {
        return "Help for " + getName();
    }

    public final HelpTopic getHelpTopic() {
        String name = getHelpTopicName();
        return name == null ? null : FlexPlugin.getRegisteredModule(HelpManager.class).getHelpTopic(name);
    }

}