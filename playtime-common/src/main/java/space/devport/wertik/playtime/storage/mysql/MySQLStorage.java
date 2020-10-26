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
import space.devport.wertik.playtime.system.TopCache;
import space.devport.wertik.playtime.utils.CommonUtility;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
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
                    if (CommonLogger.getImplementation().isDebug())
                        exc.printStackTrace();
                    return null;
                });
    }

    @Override
    public CompletableFuture<List<User>> getTop(int count) {
        return CompletableFuture.supplyAsync(() -> {
            ResultSet resultSet = connection.executeQuery(Query.GET_TOP.get(tableName).replace("%count%", String.valueOf(count)));

            List<User> top = new LinkedList<>();
            try {
                while (resultSet.next()) {
                    UUID uniqueID = convertUUID(resultSet.getString("uuid"));
                    User user = new User(uniqueID);
                    user.setPlayedTime(resultSet.getLong("time"));
                    user.setLastKnownName(resultSet.getString("lastKnownName"));
                    top.add(user);
                }
            } catch (SQLException exception) {
                throw new CompletionException(exception);
            }
            return top;
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
            if (CommonLogger.getImplementation().isDebug())
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
            if (CommonLogger.getImplementation().isDebug())
                exc.printStackTrace();

            // Name fallback
            String name = CommonUtility.getImplementation().getPlayerName(uniqueID);
            return loadUser(name).join();
        });
    }

    @Override
    public Set<User> loadAll() {
        throw new NotImplementedException();
    }

    @Override
    public void saveUser(User user) {
        CompletableFuture.runAsync(() -> connection.execute(Query.UPDATE_USER.get(tableName),
                user.getUniqueID().toString(),
                user.getLastKnownName(),
                user.getPlayedTime(),
                user.getUniqueID().toString(),
                user.getLastKnownName(),
                user.getPlayedTime()))
                .exceptionally((exc) -> {
                    if (CommonLogger.getImplementation().isDebug())
                        exc.printStackTrace();
                    return null;
                });
    }

    @Override
    public void deleteUser(User user) {
        CompletableFuture.runAsync(() -> connection.execute(Query.DELETE_USER.get(tableName), user.getUniqueID().toString()))
                .exceptionally((exc) -> {
                    if (CommonLogger.getImplementation().isDebug())
                        exc.printStackTrace();
                    return null;
                });
    }

    @Override
    public void purge() {
        CompletableFuture.runAsync(() -> connection.execute(Query.DROP_TABLE.get(tableName)))
                .exceptionally((exc) -> {
                    if (CommonLogger.getImplementation().isDebug())
                        exc.printStackTrace();
                    return null;
                });
    }

    @Override
    public void purge(Predicate<User> conditions) {
        throw new NotImplementedException();
    }
}