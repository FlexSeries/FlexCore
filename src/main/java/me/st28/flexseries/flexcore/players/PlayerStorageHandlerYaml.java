package me.st28.flexseries.flexcore.players;

import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Timestamp;
import java.util.UUID;

/**
 * A YAML-based storage layer for the {@link me.st28.flexseries.flexcore.players.PlayerManager}.
 */
final class PlayerStorageHandlerYaml extends PlayerStorageHandler {

    final static String CONFIG_FIRST_JOIN = "firstJoin";
    final static String CONFIG_LAST_LOGIN = "lastLogin";
    final static String CONFIG_LAST_LOGOUT = "lastLogout";

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

        return data;
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

        file.save();
    }

}