package space.devport.wertik.playtime.storage.mysql;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import space.devport.wertik.playtime.CommonUtility;
import space.devport.wertik.playtime.ServerConnection;
import space.devport.wertik.playtime.TaskChainFactoryHolder;
import space.devport.wertik.playtime.console.AbstractConsoleOutput;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.struct.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class MySQLStorage implements IUserStorage {

    @Getter
    private final ServerConnection connection;

    @Getter
    @Setter
    private String tableName;

    public MySQLStorage(ServerConnection connection, String tableName) {
        this.connection = connection;
        this.tableName = tableName;
    }

    @Override
    public void initialize() {
        TaskChainFactoryHolder.newChain().async(() -> connection.execute(Query.CREATE_TABLE.get(tableName))).execute((exception, task) -> {
            if (exception != null) exception.printStackTrace();
            AbstractConsoleOutput.getImplementation().debug("MySQL storage initialized.");
        });
    }

    /**
     * Name fallback query.
     */
    private long getTimeByName(String name) {
        AbstractConsoleOutput.getImplementation().debug("Falling back to name, " + name);
        ResultSet resultSet = connection.executeQuery(Query.GET_TIME_NAME.get(tableName), name);

        long time = 0;
        if (resultSet != null)
            try {
                if (resultSet.next()) {
                    time = resultSet.getLong("time");
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        return time;
    }

    //TODO Fallback even when the name isn't the same. There's little to no chance to UUID duplication, but just in case.
    @Override
    public User loadUser(UUID uniqueID) {

        if (!exists(uniqueID)) return null;

        User user = new User(uniqueID);

        long time = 0;

        ResultSet resultSet = connection.executeQuery(Query.GET_TIME.get(tableName), uniqueID.toString());

        try {
            if (resultSet == null || !resultSet.next()) {
                String name = CommonUtility.getImplementation().getOfflinePlayerName(uniqueID);
                if (name != null)
                    time = getTimeByName(name);
            } else
                time = resultSet.getLong("time");
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        user.setPlayedTime(time);
        return user;
    }

    @Override
    public Set<User> loadAll() {
        //TODO
        return new HashSet<>();
    }

    @Override
    public void saveUser(User user) {
        String lastKnownName = Bukkit.getOfflinePlayer(user.getUniqueID()).getName();

        boolean exists = exists(user.getUniqueID());

        if (exists)
            TaskChainFactoryHolder.newChain().async(() -> connection.execute(Query.UPDATE_USER.get(tableName),
                    user.getUniqueID().toString(),
                    lastKnownName,
                    user.getPlayedTime()))
                    .execute((exception, task) -> {
                        if (exception != null) exception.printStackTrace();
                    });
        else
            TaskChainFactoryHolder.newChain().async(() -> connection.execute(Query.INSERT_USER.get(tableName),
                    user.getUniqueID().toString(),
                    lastKnownName,
                    user.getPlayedTime()))
                    .execute((exception, task) -> {
                        if (exception != null) exception.printStackTrace();
                    });
    }

    @Override
    public void deleteUser(User user) {
        TaskChainFactoryHolder.newChain().async(() -> connection.execute(Query.DELETE_USER.get(tableName), user.getUniqueID().toString()))
                .execute((exception, task) -> {
                    if (exception != null) exception.printStackTrace();
                });
    }

    @Override
    public void purge() {
        TaskChainFactoryHolder.newChain().async(() -> connection.execute(Query.DROP_TABLE.get(tableName)))
                .execute((exception, task) -> {
                    if (exception != null) exception.printStackTrace();
                });
    }

    @Override
    public void purge(Function<User, Boolean> conditions) {
        throw new NotImplementedException();
    }

    /**
     * Exists name fallback.
     */
    private boolean existsByName(String name) {
        AbstractConsoleOutput.getImplementation().debug("Falling back to names, " + name);
        try {
            ResultSet rs = connection.executeQuery(Query.EXIST_CHECK_NAME.get(tableName), name);
            if (rs.next()) return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Check if the table has this entry.
     */
    public boolean exists(UUID uuid) {
        try {
            ResultSet rs = connection.executeQuery(Query.EXIST_CHECK.get(tableName), uuid.toString());
            if (rs.next()) return true;
            else {
                String name = CommonUtility.getImplementation().getOfflinePlayerName(uuid);
                if (name != null)
                    return existsByName(name);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}