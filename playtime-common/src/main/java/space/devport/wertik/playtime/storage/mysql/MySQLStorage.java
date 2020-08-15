package space.devport.wertik.playtime.storage.mysql;

import com.sun.rowset.CachedRowSetImpl;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.glaremasters.playertime.database.DatabaseProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import space.devport.wertik.playtime.console.AbstractConsoleOutput;
import space.devport.wertik.playtime.PlayTimeCommons;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.struct.User;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.sql.rowset.CachedRowSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@NoArgsConstructor
public class MySQLStorage implements IUserStorage {

    private HikariDataSource hikari;

    @Getter
    @Setter
    private String host;
    @Getter
    @Setter
    private int port;

    @Getter
    @Setter
    private String databaseName;
    @Getter
    @Setter
    private String tableName;
    @Getter
    @Setter
    private String user;
    @Getter
    @Setter
    private String password;

    @Getter
    @Setter
    private int poolSize;

    @Override
    public void initialize() {

        if (host == null) {
            throw new IllegalStateException("MySQL not configured correctly. Cannot continue.");
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

        PlayTimeCommons.newChain().async(() -> execute(Query.CREATE_TABLE.get(tableName))).execute((exception, task) -> {
            if (exception != null) exception.printStackTrace();
        });
    }

    @Override
    public User loadUser(UUID uniqueID) {
        return null;
    }

    @Override
    public Set<User> loadAll() {
        return null;
    }

    @Override
    public void saveUser(User user) {

    }

    @Override
    public void saveAll(Set<User> users) {

    }

    @Override
    public void deleteUser(User user) {

    }

    @Override
    public void purge() {

    }

    @Override
    public void purge(Function<User, Boolean> conditions) {
        throw new NotImplementedException();
    }

    @Override
    public void insertUser(UUID uuid, String time) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        PlayTimeCommons.newChain().async(() -> execute(Query.INSERT_USER, uuid.toString(), offlinePlayer.getName(), time)).execute((exception, task) -> {
            if (exception != null) exception.printStackTrace();
        });
    }

    @Override
    public boolean hasTime(UUID uuid) {
        try {
            ResultSet rs = executeQuery(Query.EXIST_CHECK, uuid.toString());
            if (rs.next()) return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public Map<String, Integer> getTopTen() {
        Map<String, Integer> topTen = new LinkedHashMap<>();
        try {
            ResultSet rs = executeQuery(Query.GET_TOP_TEN.get(tableName));
            if (rs == null) return topTen;
            topTen.put(rs.getString("uuid"), Integer.valueOf(rs.getString("time")));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return topTen;
    }

    public void setTime(UUID uuid, String time) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        PlayTimeCommons.newChain().async(() -> execute(Query.UPDATE_USER.get(tableName), uuid.toString(), offlinePlayer.getName(), time)).execute((exception, task) -> {
            if (exception != null) exception.printStackTrace();
        });
    }

    public String getTime(UUID uuid) {
        try {
            ResultSet rs = executeQuery(Query.GET_TIME, uuid.toString());
            if (rs == null) return "";
            return rs.getString("time");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    private void execute(String query, Object... parameters) {

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

    private ResultSet executeQuery(String query, Object... parameters) {

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