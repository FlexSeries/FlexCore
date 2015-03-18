package me.st28.flexseries.flexcore.plugins;

import me.st28.flexseries.flexcore.players.loading.PlayerLoadCycle;
import me.st28.flexseries.flexcore.players.loading.PlayerLoader;
import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents a module for a plugin.
 *
 * @param <T> The type of plugin that owns the module.
 */
public abstract class FlexModule<T extends FlexPlugin> {

    /**
     * The plugin that owns the FlexModule.
     */
    protected final T plugin;

    /**
     * The short name of the FlexModule.  Intended to reference modules in an easier way than by the normal name.
     */
    protected final String identifier;

    /**
     * The description of the FlexModule.
     */
    protected final String description;

    /**
     * Dependencies on other modules from FlexPlugin implementations.<br />
     * Format: Plugin name$Module short name
     */
    private final List<Class<? extends FlexModule>> dependencies = new ArrayList<>();

    /**
     * Whether or not the module can be disabled via config.
     */
    protected boolean isDisableable = true;

    /**
     * Whether or not to use a data folder.<br />
     * If false, the config file will be located in <code>(plugin base dir)\config-(module identifier).yml</code>
     */
    private boolean useModuleFolder;

    /**
     * The directory for this module's data.
     */
    private File dataFolder;

    /**
     * The config file for the module.
     */
    private YamlFileManager configFile;

    public FlexModule(T plugin, String identifier, String description, boolean useModuleFolder, Class<? extends FlexModule>... dependencies) {
        this.plugin = plugin;
        this.identifier = identifier;
        this.description = description;
        this.useModuleFolder = useModuleFolder;
        Collections.addAll(this.dependencies, dependencies);
    }

    /**
     * @return the {@link me.st28.flexseries.flexcore.plugins.FlexPlugin} that this module is registered under.
     */
    public final T getPlugin() {
        return plugin;
    }

    /**
     * @return the status of the module.
     */
    public final ModuleStatus getStatus() {
        return plugin.getModuleStatus(getClass());
    }

    /**
     * @return the short name of the module.<br />
     *         Should not contain spaces or symbols for easier reference.
     */
    public final String getIdentifier() {
        return identifier;
    }

    /**
     * @return the description of the module.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * @return read-only collection of module dependencies for this module.
     */
    public final Collection<Class<? extends FlexModule>> getDependencies() {
        return Collections.unmodifiableCollection(dependencies);
    }

    /**
     * @return true if the module can be disabled via the plugin's configuration file.
     */
    public final boolean isDisableable() {
        return isDisableable;
    }

    /**
     * Reloads the configuration file for the module.
     */
    public final void reloadConfig() {
        if (configFile != null) {
            configFile.reload();
            FileConfiguration config = configFile.getConfig();

            config.addDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("modules/" + identifier + "/config.yml"))));
            config.options().copyDefaults(true);
            configFile.save();
            configFile.reload();
        }
    }

    /**
     * Saves the module's configuration file.
     */
    public final void saveConfig() {
        if (configFile != null) {
            configFile.save();
        }
    }

    /**
     * @return the data folder for the module.<br />
     *         If {@link #useModuleFolder} is <code>false</code>, then this will return the owning plugin's base data folder.
     */
    public final File getDataFolder() {
        if (!useModuleFolder) {
            return plugin.getDataFolder();
        }

        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        return dataFolder;
    }

    /**
     * @return the configuration file for this module.<br />
     *         Null if there is no configuration file for this module.
     */
    public final FileConfiguration getConfig() {
        return configFile == null ? null : configFile.getConfig();
    }

    /**
     * Loads the module.
     */
    public final void loadAll() {
        if (useModuleFolder) {
            dataFolder = new File(plugin.getDataFolder() + File.separator + identifier);
            dataFolder.mkdirs();
        }

        if (plugin.getResource("modules/" + identifier + "/config.yml") != null) {
            if (useModuleFolder) {
                configFile = new YamlFileManager(dataFolder + File.separator + "config.yml");
            } else {
                configFile = new YamlFileManager(plugin.getDataFolder() + File.separator + "config-" + getIdentifier() + ".yml");
            }
        } else {
            configFile = null;
        }

        reloadConfig();
        handleLoad();
        handleReload();

        if (this instanceof Listener) {
            Bukkit.getPluginManager().registerEvents((Listener) this, plugin);
        }

        if (this instanceof PlayerLoader) {
            PlayerLoadCycle.registerLoader((PlayerLoader) this);
        }
    }

    /**
     * Reloads all of the module.
     */
    public final void reloadAll() {
        reloadConfig();
        handleReload();
    }

    /**
     * Saves all of the module data.
     */
    public final void saveAll(boolean async) {
        saveConfig();
        handleSave(async);
    }

    /**
     * Disables the module.
     */
    public final void disable() {
        saveAll(false);
        handleDisable();
    }

    /**
     * Handles module custom load tasks. This will only be called when enabling the module.
     */
    protected void handleLoad() {}

    /**
     * Handles module custom reload tasks.
     */
    protected void handleReload() {}

    /**
     * Handles module custom save tasks.
     *
     * @param async If true, should save asynchronously (if applicable).
     */
    protected void handleSave(boolean async) {}

    /**
     * Handles module custom disable tasks.
     */
    protected void handleDisable() {}

}