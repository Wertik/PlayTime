package space.devport.wertik.playtime.system;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import space.devport.wertik.playtime.ConnectionInfo;
import space.devport.wertik.playtime.ConnectionManager;
import space.devport.wertik.playtime.ServerConnection;
import space.devport.wertik.playtime.console.AbstractConsoleOutput;
import space.devport.wertik.playtime.storage.mysql.MySQLStorage;
import space.devport.wertik.playtime.struct.GlobalUser;
import space.devport.wertik.playtime.struct.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class GlobalUserManager {

    //TODO Cache is useless right now, make it not.
    private final Map<UUID, GlobalUser> loadedUsers = new HashMap<>();

    private final Map<String, MySQLStorage> storages = new HashMap<>();

    /**
     * @param serverName Name of the server and the table to connect to.
     */
    public boolean initializeStorage(String serverName, ConnectionInfo connectionInfo, String tableName) {
        if (this.storages.containsKey(serverName)) return false;

        ServerConnection connection = ConnectionManager.getInstance().initializeConnection(serverName, connectionInfo);

        if (connection == null) {
            AbstractConsoleOutput.getImplementation().err("Could not initialize remote storage " + serverName + " with table " + tableName);
            return false;
        } else {
            MySQLStorage storage = new MySQLStorage(connection, tableName);
            this.storages.put(serverName, storage);
            AbstractConsoleOutput.getImplementation().info("Initialized remote storage for server " + serverName + " with table " + tableName);
            return true;
        }
    }

    @NotNull
    private GlobalUser getOrCreateGlobalUser(UUID uniqueID) {

        GlobalUser user;
        if (!this.loadedUsers.containsKey(uniqueID)) {
            user = new GlobalUser(uniqueID);
            this.loadedUsers.put(uniqueID, user);
        } else user = this.loadedUsers.get(uniqueID);

        return user;
    }

    /**
     * Update data, if not loaded, create new.
     */
    @NotNull
    public GlobalUser fetchGlobalUser(UUID uniqueID) {
        GlobalUser user = getOrCreateGlobalUser(uniqueID);

        for (Map.Entry<String, MySQLStorage> entry : storages.entrySet()) {
            User remoteUser = entry.getValue().loadUser(uniqueID);
            if (remoteUser == null) continue;
            user.updateRecord(entry.getKey(), remoteUser);
        }

        AbstractConsoleOutput.getImplementation().debug("Updated global user " + user.getUniqueID());
        return user;
    }

    public long fetchPlayTime(UUID uniqueID, String serverName) {
        GlobalUser globalUser = fetchGlobalUser(uniqueID);
        return globalUser.getPlayedTime(serverName);
    }

    public void unload(UUID uniqueID) {
        this.loadedUsers.remove(uniqueID);
    }
}