package space.devport.wertik.playtime.storage.mysql;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import space.devport.wertik.playtime.NotImplementedException;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Predicate;

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
        CompletableFuture.runAsync(() -> connection.execute(Query.CREATE_TABLE.get(tableName)))
                .thenRun(() -> CommonLogger.getImplementation().debug("MySQL storage initialized."))
                .exceptionally((exc) -> {
                    exc.printStackTrace();
                    return null;
                });
    }

    @Override
    public CompletableFuture<User> loadUser(String name) {
        return CompletableFuture.supplyAsync(() -> {
            ResultSet resultSet = connection.executeQuery(Query.GET_USER_BY_NAME.get(tableName), name);

            long time = 0;
            UUID uniqueID = null;
            try {
                if (resultSet.next()) {
                    time = resultSet.getLong("time");
                    uniqueID = convertUUID(resultSet.getString("uuid"));
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }

            if (uniqueID == null) {
                CommonLogger.getImplementation().debug("There is no user with the name " + name + " saved.");
                return null;
            }

            User user = new User(uniqueID);
            user.setLastKnownName(name);
            user.setPlayedTime(time);
            return user;
        }).exceptionally((exc) -> {
            exc.printStackTrace();
            return null;
        });
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
    public CompletableFuture<User> loadUser(UUID uniqueID) {
        return CompletableFuture.supplyAsync(() -> {
            ResultSet resultSet = connection.executeQuery(Query.GET_USER.get(tableName), uniqueID.toString());

            User user = new User(uniqueID);
            try {
                if (resultSet.next()) {
                    long time = resultSet.getLong("time");
                    String name = resultSet.getString("lastKnownName");

                    user.setLastKnownName(name);
                    user.setPlayedTime(time);
                } else return null;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
            return user;
        }).exceptionally((exc) -> {
            // Name fallback
            String name = CommonUtility.getImplementation().getOfflinePlayerName(uniqueID);
            return loadUser(name).join();
        });
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
            CompletableFuture.runAsync(() -> connection.execute(Query.UPDATE_USER.get(tableName),
                    user.getUniqueID().toString(),
                    lastKnownName,
                    user.getPlayedTime())).exceptionally((exc) -> {
                exc.printStackTrace();
                return null;
            });
        else
            CompletableFuture.runAsync(() -> connection.execute(Query.INSERT_USER.get(tableName),
                    user.getUniqueID().toString(),
                    lastKnownName,
                    user.getPlayedTime()))
                    .exceptionally((exc) -> {
                        exc.printStackTrace();
                        return null;
                    });
    }

    @Override
    public void deleteUser(User user) {
        CompletableFuture.runAsync(() -> connection.execute(Query.DELETE_USER.get(tableName), user.getUniqueID().toString()))
                .exceptionally((exc) -> {
                    exc.printStackTrace();
                    return null;
                });
    }

    @Override
    public void purge() {
        CompletableFuture.runAsync(() -> connection.execute(Query.DROP_TABLE.get(tableName)))
                .exceptionally((exc) -> {
                    exc.printStackTrace();
                    return null;
                });
    }

    @Override
    public void purge(Predicate<User> conditions) {
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
            if (CommonLogger.getImplementation().isDebug())
                ex.printStackTrace();
        }
        return false;
    }

    /**
     * Check if the table has this entry.
     */
    //TODO remove
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
            if (CommonLogger.getImplementation().isDebug())
                ex.printStackTrace();
        }
        return false;
    }
}