package space.devport.wertik.playtime;

import com.sun.rowset.CachedRowSetImpl;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import space.devport.wertik.playtime.console.AbstractConsoleOutput;

import javax.sql.rowset.CachedRowSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLConnection {

    private HikariDataSource hikari;

    @Getter
    @Setter
    private String host;
    @Getter
    @Setter
    private int port;

    @Getter
    @Setter
    private String user;
    @Getter
    @Setter
    private String password;

    @Getter
    @Setter
    private String databaseName;

    @Getter
    @Setter
    private int poolSize;

    public MySQLConnection(String host, int port, String user, String password, String databaseName, int poolSize) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.databaseName = databaseName;
        this.poolSize = poolSize;
    }

    public void connect() {

        if (host == null) {
            throw new IllegalStateException("MySQL Connection not configured. Cannot continue.");
        }

        hikari = new HikariDataSource();
        hikari.setMaximumPoolSize(poolSize);

        hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");

        hikari.addDataSourceProperty("serverName", host);
        hikari.addDataSourceProperty("port", port);
        hikari.addDataSourceProperty("databaseName", databaseName);

        hikari.addDataSourceProperty("user", user);
        hikari.addDataSourceProperty("password", password);

        hikari.addDataSourceProperty("characterEncoding", "utf8");
        hikari.addDataSourceProperty("useUnicode", "true");

        hikari.validate();
    }

    public void execute(String query, Object... parameters) {

        try (Connection connection = hikari.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    statement.setObject(i + 1, parameters[i]);
                }
            }

            statement.execute();
            AbstractConsoleOutput.getImplementation().debug("Executed statement " + statement.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet executeQuery(String query, Object... parameters) {

        try (Connection connection = hikari.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            if (parameters != null) {
                for (int i = 0; i < parameters.length; i++) {
                    statement.setObject(i + 1, parameters[i]);
                }
            }

            CachedRowSet resultCached = new CachedRowSetImpl();
            ResultSet resultSet = statement.executeQuery();

            AbstractConsoleOutput.getImplementation().debug("Executed query " + statement.toString());

            resultCached.populate(resultSet);
            resultSet.close();

            return resultCached;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}