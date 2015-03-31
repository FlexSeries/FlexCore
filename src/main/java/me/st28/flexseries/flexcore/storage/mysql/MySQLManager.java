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