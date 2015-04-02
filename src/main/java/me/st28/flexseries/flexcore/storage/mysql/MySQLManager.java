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
package me.st28.flexseries.flexcore.storage.mysql;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import me.st28.flexseries.flexcore.FlexCore;
import me.st28.flexseries.flexcore.logging.LogHelper;
import me.st28.flexseries.flexcore.plugin.module.FlexModule;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Manages a MySQL connection pool with the BoneCP library.
 */
public final class MySQLManager extends FlexModule<FlexCore> {

    private BoneCP pool;

    private boolean dbSpecified;

    public MySQLManager(FlexCore plugin) {
        super(plugin, "storage-mysql", "Manages a MySQL connection pool", false);
    }

    @Override
    public final void handleLoad() {
        LogHelper.info(this, "Setting up database.");

        ConfigurationSection dbConf = getConfig().getConfigurationSection("Database");
        String host = dbConf.getString("host", "localhost");
        int port = dbConf.getInt("port", 3306);
        String db = dbConf.getString("db", "");
        if (db.equals("")) db = null;
        dbSpecified = db != null;
        String user = dbConf.getString("user");
        String pass = dbConf.getString("pass");
        int minConnections = dbConf.getInt("bonecp.minConnections", 5);
        int maxConnections = dbConf.getInt("bonecp.maxConnections", 10);
        int partitions = dbConf.getInt("bonecp.partitions", 1);

        if (db == null || user == null || pass == null) {
            throw new IllegalArgumentException("MySQL Database name, user, or pass are not defined in config.yml");
        }

        BoneCPConfig cpConfig = new BoneCPConfig();
        cpConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + (!dbSpecified ? "" : ("/" + db)));
        cpConfig.setUsername(user);
        cpConfig.setPassword(pass);
        cpConfig.setMinConnectionsPerPartition(minConnections);
        cpConfig.setMaxConnectionsPerPartition(maxConnections);
        cpConfig.setPartitionCount(partitions);

        try {
            pool = new BoneCP(cpConfig);
        } catch (Exception ex) {
            LogHelper.severe(FlexCore.class, "An error occurred while trying to create the BoneCP connection pool: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /**
     * @return a connection to a particular database.
     */
    public final Connection getConnection(String database) throws SQLException {
        Connection connection = pool.getConnection();
        if (!dbSpecified) {
            PreparedStatement ps = connection.prepareStatement("USE `" + database + "`;");
            ps.executeUpdate();
            ps.close();
        }
        return connection;
    }

}