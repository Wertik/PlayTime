package space.devport.wertik.playtime.storage.mysql;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import space.devport.wertik.playtime.NotImplementedException;
import space.devport.wertik.playtime.TaskChainFactoryHolder;
import space.devport.wertik.playtime.console.CommonLogger;
import space.devport.wertik.playtime.mysql.struct.ServerConnection;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.struct.User;
import space.devport.wertik.playtime.utils.CommonUtility;

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

    @Getter
    private boolean networkServer = false;

    public MySQLStorage(ServerConnection connection, String tableName) {
        this.connection = connection;
        this.tableName = tableName;
    }

    public MySQLStorage(ServerConnection connection, String tableName, boolean networkServer) {
        this.connection = connection;
        this.tableName = tableName;
        this.networkServer = networkServer;
    }

    @Override
    public void initialize() {
        TaskChainFactoryHolder.newChain().async(() -> connection.execute(Query.CREATE_TABLE.get(tableName))).execute((exception, task) -> {
            if (exception != null) exception.printStackTrace();
            CommonLogger.getImplementation().debug("MySQL storage initialized.");
        });
    }

    @Override
    public User loadUser(String name) {

        if (!exists(name))
            return null;

        ResultSet resultSet = connection.executeQuery(Query.GET_USER_BY_NAME.get(tableName), name);

        long time = 0;
        UUID uniqueID = null;

        if (resultSet != null)
            try {
                if (resultSet.next()) {
                    time = resultSet.getLong("time");
                    uniqueID = convertUUID(resultSet.getString("uuid"));
                }
            } catch (SQLException exception) {
                exception.printStackTrace();
            }

        if (uniqueID == null) return null;

        User user = new User(uniqueID);
        user.setPlayedTime(time);

        return user;
    }

    @Nullable
    private UUID convertUUID(String stringUUID) {
        if (Strings.isNullOrEmpty(stringUUID)) return null;

        try {
            return UUID.fromString(stringUUID);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public User loadUser(UUID uniqueID) {

        if (!exists(uniqueID)) return null;

        User user = new User(uniqueID);

        long time = 0;

        ResultSet resultSet = connection.executeQuery(Query.GET_USER.get(tableName), uniqueID.toString());

        try {
            if (resultSet == null || !resultSet.next()) {
                // Name fallback
                String name = CommonUtility.getImplementation().getOfflinePlayerName(uniqueID);
                return loadUser(name);
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
        String lastKnownName = CommonUtility.getImplementation().getOfflinePlayerName(user.getUniqueID());

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

    private boolean exists(String name) {

        if (name == null)
            return false;

        ResultSet resultSet = connection.executeQuery(Query.EXIST_CHECK_NAME.get(tableName), name);

        try {
            if (resultSet.next())
                return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Check if the table has this entry.
     */
    public boolean exists(UUID uuid) {
        ResultSet resultSet = connection.executeQuery(Query.EXIST_CHECK.get(tableName), uuid.toString());

        try {
            if (resultSet.next()) return true;
            else {
                // Name fallback
                String name = CommonUtility.getImplementation().getOfflinePlayerName(uuid);
                return exists(name);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}