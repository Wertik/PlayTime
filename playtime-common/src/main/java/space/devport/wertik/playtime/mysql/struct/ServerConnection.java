package space.devport.wertik.playtime.mysql.struct;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import space.devport.wertik.playtime.console.CommonLogger;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ServerConnection {

    @Getter
    private final ConnectionInfo connectionInfo;

    private HikariDataSource hikari;

    @Getter
    private boolean connected = false;

    public ServerConnection(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }

    public void connect() {

        if (connectionInfo.getHost() == null) {
            throw new IllegalStateException("MySQL Connection not configured. Cannot continue.");
        }

        this.hikari = new HikariDataSource();
        hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");

        hikari.addDataSourceProperty("serverName", connectionInfo.getHost());
        hikari.addDataSourceProperty("port", connectionInfo.getPort());
        hikari.addDataSourceProperty("databaseName", connectionInfo.getDatabase());

        hikari.addDataSourceProperty("user", connectionInfo.getUsername());
        hikari.addDataSourceProperty("password", connectionInfo.getPassword());

        hikari.addDataSourceProperty("characterEncoding", "utf8");
        hikari.addDataSourceProperty("useUnicode", "true");

        hikari.setReadOnly(connectionInfo.isReadOnly());

        try {
            hikari.validate();
        } catch (IllegalStateException e) {
            throw new IllegalStateException(e);
        }

        Connection connection;
        try {
            connection = hikari.getConnection();
        } catch (SQLException exception) {
            throw new IllegalStateException(exception);
        }

        if (connection == null)
            throw new IllegalStateException("No connection");

        this.connected = true;
    }

    public void execute(String query, Object... parameters) {

        if (!isConnected()) return;

        try (Connection connection = hikari.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    statement.setObject(i + 1, parameters[i]);
                }
            }

            statement.execute();
            CommonLogger.getImplementation().debug("Executed statement " + statement.toString());
        } catch (SQLException e) {
            if (CommonLogger.getImplementation().isDebug())
                e.printStackTrace();
        }
    }

    public ResultSet executeQuery(String query, Object... parameters) {

        if (!isConnected()) return null;

        try (Connection connection = hikari.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    statement.setObject(i + 1, parameters[i]);
                }
            }

            CachedRowSet resultCached = RowSetProvider.newFactory().createCachedRowSet();
            ResultSet resultSet = statement.executeQuery();

            CommonLogger.getImplementation().debug("Executed query " + statement.toString());

            resultCached.populate(resultSet);
            resultSet.close();

            return resultCached;
        } catch (SQLException e) {
            if (CommonLogger.getImplementation().isDebug())
                e.printStackTrace();
        }

        return null;
    }

    public void close() {
        this.hikari.close();
    }
}