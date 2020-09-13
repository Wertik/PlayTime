package space.devport.wertik.playtime;

import com.zaxxer.hikari.HikariDataSource;
import space.devport.wertik.playtime.console.AbstractConsoleOutput;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySQLConnection extends ConnectionInfo {

    public MySQLConnection(String host, int port, String username, String password, String database) {
        super(host, port, username, password, database);
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

            CachedRowSet resultCached = RowSetProvider.newFactory().createCachedRowSet();
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