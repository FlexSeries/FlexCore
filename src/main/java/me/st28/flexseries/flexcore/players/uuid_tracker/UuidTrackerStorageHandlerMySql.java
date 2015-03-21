package me.st28.flexseries.flexcore.players.uuid_tracker;

import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.plugins.FlexPlugin;
import me.st28.flexseries.flexcore.storage.mysql.MySQLManager;
import me.st28.flexseries.flexcore.utils.UuidUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

final class UuidTrackerStorageHandlerMySql extends UuidTrackerStorageHandler {

    private static String STATEMENT_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS ? (`uuid` BINARY(16), `name` VARCHAR(16), PRIMARY KEY (`uuid`));";
    private static String STATEMENT_LOAD_ENTRY = "SELECT HEX(`uuid`), `name` FROM ?;";
    private static String STATEMENT_SAVE_ENTRY = "INSERT INTO ? (`uuid`, `name`) VALUES (UNHEX(?), ?) ON DUPLICATE KEY UPDATE `name` = ?;";

    String database;
    String table;

    UuidTrackerStorageHandlerMySql(PlayerUuidTracker uuidTracker, String database, String prefix) {
        super(uuidTracker, false);
        this.database = database;
        table = (prefix == null ? "" : prefix) + "uuid_index";
    }

    @Override
    Map<UUID, String> loadIndex() {
        try (Connection connection = FlexPlugin.getRegisteredModule(MySQLManager.class).getConnection(database)) {
            final Map<UUID, String> index = new HashMap<>();

            PreparedStatement ps = connection.prepareStatement(STATEMENT_CREATE_TABLE);
            ps.setString(1, table);
            ps.executeUpdate();
            ps.close();

            ps = connection.prepareStatement(STATEMENT_LOAD_ENTRY);
            ps.setString(1, table);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String rawUuid = rs.getString(1);
                String name = rs.getString(2);

                UUID uuid;
                try {
                    uuid = UuidUtils.fromStringNoDashes(rawUuid);
                } catch (Exception ex) {
                    LogHelper.warning(uuidTracker, "Invalid UUID in the '" + table + "' database table: '" + rawUuid + "'");
                    continue;
                }

                index.put(uuid, name);
            }

            rs.close();
            ps.close();

            return index;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    void saveIndex(Map<UUID, String> index) {
        try (Connection connection = FlexPlugin.getRegisteredModule(MySQLManager.class).getConnection(database)) {
            PreparedStatement ps = connection.prepareStatement(STATEMENT_CREATE_TABLE);
            ps.setString(1, table);
            ps.executeUpdate();
            ps.close();

            ps = connection.prepareStatement(STATEMENT_SAVE_ENTRY);
            for (Entry<UUID, String> entry : index.entrySet()) {
                ps.setString(1, table);
                ps.setString(2, entry.getKey().toString().replace("-", ""));
                ps.setString(3, entry.getValue());
                ps.setString(4, entry.getValue());
                ps.addBatch();
            }
            ps.executeBatch();

            ps.close();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

}