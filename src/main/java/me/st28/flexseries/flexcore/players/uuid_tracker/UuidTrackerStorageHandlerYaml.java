package me.st28.flexseries.flexcore.players.uuid_tracker;

import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.storage.flatfile.YamlFileManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

final class UuidTrackerStorageHandlerYaml extends UuidTrackerStorageHandler {

    UuidTrackerStorageHandlerYaml(PlayerUuidTracker uuidTracker) {
        super(uuidTracker, false);
    }

    private YamlFileManager getFile() {
        return new YamlFileManager(uuidTracker.getPlugin().getDataFolder() + File.separator + "players" + File.separator + "uuidNameIndex.yml");
    }

    @Override
    Map<UUID, String> loadIndex() {
        final Map<UUID, String> index = new HashMap<>();
        FileConfiguration config = getFile().getConfig();

        for (String rawUuid : config.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(rawUuid);
            } catch (Exception ex) {
                LogHelper.warning(uuidTracker, "Invalid UUID in players/uuidNameIndex.yml: '" + rawUuid + "'");
                continue;
            }

            index.put(uuid, config.getString(rawUuid));
        }

        return index;
    }

    @Override
    void saveIndex(Map<UUID, String> index) {
        YamlFileManager file = getFile();
        FileConfiguration config = file.getConfig();

        for (Entry<UUID, String> entry : index.entrySet()) {
            config.set(entry.getKey().toString(), entry.getValue());
        }

        file.save();
    }

}