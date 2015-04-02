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
package me.st28.flexseries.flexcore.player.uuid_tracker;

import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.plugin.FlexPlugin;
import me.st28.flexseries.flexcore.storage.mysql.MySQLManager;
import me.st28.flexseries.flexcore.util.UuidUtils;

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