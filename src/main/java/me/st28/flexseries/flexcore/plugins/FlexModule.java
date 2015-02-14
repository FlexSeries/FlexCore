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
     * The config file for the module.
     */
    private YamlFileManager configFile;

    public FlexModule(T plugin, String identifier, String description, Class<? extends FlexModule>... dependencies) {
        this.plugin = plugin;
        this.identifier = identifier;
        this.description = description;
        Collections.addAll(this.dependencies, dependencies);
    }

    public T getPlugin() {
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

            config.addDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("modules/" + identifier + ".yml"))));
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

    public final FileConfiguration getConfig() {
        return configFile == null ? null : configFile.getConfig();
    }

    public final void loadAll() throws Exception {
        if (plugin.getResource("modules/" + identifier + ".yml") != null) {
            configFile = new YamlFileManager(plugin.getDataFolder() + File.separator + "modules" + File.separator + identifier + ".yml");
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
     * Handles module custom load tasks. This will only be called when enabling the module.
     *
     * @throws Exception Any exceptions that may be thrown while loading.
     */
    protected void handleLoad() throws Exception { }

    /**
     * Reloads all of the module.
     */
    public final void reloadAll() {
        reloadConfig();
        handleReload();
    }

    /**
     * Handles module custom reload tasks.
     */
    protected void handleReload() { }

    /**
     * Saves all of the module data.
     */
    public final void saveAll(boolean async) {
        saveConfig();
        handleSave(async);
    }

    /**
     * Handles module custom save tasks.
     *
     * @param async If true, should save asynchronously (if applicable).
     */
    protected void handleSave(boolean async) { }

    /**
     * Disables the module.
     */
    public final void disable() {
        saveAll(false);
        handleDisable();
    }

    /**
     * Handles module custom disable tasks.
     */
    protected void handleDisable() { }

}