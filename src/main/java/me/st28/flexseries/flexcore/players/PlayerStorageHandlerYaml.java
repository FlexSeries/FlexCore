package me.st28.flexseries.flexcore.players;

import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Timestamp;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * A YAML-based storage layer for the {@link me.st28.flexseries.flexcore.players.PlayerManager}.
 */
final class PlayerStorageHandlerYaml extends PlayerStorageHandler {

    final static String CONFIG_FIRST_JOIN = "firstJoin";
    final static String CONFIG_LAST_LOGIN = "lastLogin";
    final static String CONFIG_LAST_LOGOUT = "lastLogout";
    final static String CONFIG_LAST_IP = "lastIp";
    final static String CONFIG_IPS = "ips";
    final static String CONFIG_LAST_NAME = "lastName";
    final static String CONFIG_NAMES = "names";
    final static String CONFIG_CUSTOM_DATA_SEC = "customData";

    PlayerStorageHandlerYaml(PlayerManager playerManager) {
        super(playerManager, false);
    }

    private YamlFileManager getPlayerFile(UUID uuid) {
        return new YamlFileManager(playerManager.getPlayerDir() + File.separator + uuid.toString() + ".yml");
    }

    @Override
    PlayerData createPlayerData(UUID uuid) {
        getPlayerFile(uuid).reload();
        return new PlayerData(uuid);
    }

    @Override
    PlayerData loadPlayerData(UUID uuid) {
        if (!new File(playerManager.getPlayerDir() + File.separator + uuid.toString() + ".yml").exists()) {
            return null;
        }

        YamlFileManager file = getPlayerFile(uuid);
        FileConfiguration config = file.getConfig();

        PlayerData data = new PlayerData(uuid);

        if (config.isSet(CONFIG_FIRST_JOIN)) {
            data.firstJoin = new Timestamp(config.getLong(CONFIG_FIRST_JOIN));
        }

        if (config.isSet(CONFIG_LAST_LOGIN)) {
            data.lastLogin = new Timestamp(config.getLong(CONFIG_LAST_LOGIN));
        }

        if (config.isSet(CONFIG_LAST_LOGOUT)) {
            data.lastLogout = new Timestamp(config.getLong(CONFIG_LAST_LOGOUT));
        }

        data.lastIp = config.getString(CONFIG_LAST_IP);
        data.ips.addAll(config.getStringList(CONFIG_IPS));

        data.lastName = config.getString(CONFIG_LAST_NAME);
        data.names.addAll(config.getStringList(CONFIG_NAMES));

        ConfigurationSection customDataSec = config.getConfigurationSection(CONFIG_CUSTOM_DATA_SEC);
        if (customDataSec != null) {
            for (String key : customDataSec.getKeys(false)) {
                loadCustomData(data, customDataSec, key);
            }
        }

        return data;
    }

    private void loadCustomData(PlayerData data, ConfigurationSection section, String key) {
        Object obj = section.get(key);
        if (obj instanceof ConfigurationSection) {
            for (String subKey : ((ConfigurationSection) obj).getKeys(false)) {
                loadCustomData(data, (ConfigurationSection) obj, key + "." + subKey);
            }
        } else {
            data.customData.put(key, obj);
        }
    }

    @Override
    void savePlayerData(PlayerData data) {
        YamlFileManager file = getPlayerFile(data.getUniqueId());
        FileConfiguration config = file.getConfig();

        if (data.firstJoin != null) {
            config.set(CONFIG_FIRST_JOIN, data.firstJoin.getTime());
        }

        if (data.lastLogin != null) {
            config.set(CONFIG_LAST_LOGIN, data.lastLogin.getTime());
        }

        if (data.lastLogout != null) {
            config.set(CONFIG_LAST_LOGOUT, data.lastLogout.getTime());
        }

        config.set(CONFIG_LAST_IP, data.lastIp);
        config.set(CONFIG_IPS, data.ips);

        config.set(CONFIG_LAST_NAME, data.lastName);
        config.set(CONFIG_NAMES, data.names);

        ConfigurationSection customDataSec = config.createSection(CONFIG_CUSTOM_DATA_SEC);

        for (Entry<String, Object> entry : data.customData.entrySet()) {
            customDataSec.set(entry.getKey(), entry.getValue());
        }

        file.save();
    }

}