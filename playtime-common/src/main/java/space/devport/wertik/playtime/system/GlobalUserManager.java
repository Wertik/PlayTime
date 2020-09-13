package space.devport.wertik.playtime.system;

import org.jetbrains.annotations.NotNull;
import space.devport.wertik.playtime.console.CommonLogger;
import space.devport.wertik.playtime.mysql.ConnectionManager;
import space.devport.wertik.playtime.mysql.struct.ConnectionInfo;
import space.devport.wertik.playtime.mysql.struct.ServerConnection;
import space.devport.wertik.playtime.storage.mysql.MySQLStorage;
import space.devport.wertik.playtime.struct.GlobalUser;
import space.devport.wertik.playtime.struct.ServerInfo;
import space.devport.wertik.playtime.struct.User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GlobalUserManager {

    private final Map<UUID, GlobalUser> loadedUsers = new HashMap<>();

    private final Map<String, MySQLStorage> remoteStorages = new HashMap<>();

    public GlobalUserManager() {
        DataManager.getInstance().setGlobalUserManager(this);
    }

    /**
     * @param tableName  of the table to use
     * @param serverName Name of the server and the table to connect to.
     */
    public void initializeStorage(String serverName, ConnectionInfo connectionInfo, String tableName, boolean... networkServer) {

        if (this.remoteStorages.containsKey(serverName)) return;

        ServerConnection connection = ConnectionManager.getInstance().initializeConnection(serverName, connectionInfo);

        if (connection == null) {
            CommonLogger.getImplementation().err("Could not initialize remote storage " + serverName + " with table " + tableName);
        } else {
            MySQLStorage storage = new MySQLStorage(connection, tableName, networkServer.length > 0 && networkServer[0]);

            this.remoteStorages.put(serverName, storage);
            CommonLogger.getImplementation().info("Initialized remote storage for server " + serverName + " with table " + tableName);
            if (networkServer.length > 0 && networkServer[0])
                CommonLogger.getImplementation().info("Using it as a network server.");
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

    @NotNull
    public GlobalUser getGlobalUser(UUID uniqueID) {
        GlobalUser user;
        if (!this.loadedUsers.containsKey(uniqueID))
            user = updateGlobalUser(uniqueID);
        else
            user = this.loadedUsers.get(uniqueID);
        return user;
    }

    /**
     * Update data, if not loaded, create new.
     * Called when a player joins, or the plugin is enabled and he's online.
     */
    @NotNull
    public GlobalUser updateGlobalUser(UUID uniqueID) {
        GlobalUser user = getOrCreateGlobalUser(uniqueID);

        for (Map.Entry<String, MySQLStorage> entry : remoteStorages.entrySet()) {
            User remoteUser = entry.getValue().loadUser(uniqueID);
            if (remoteUser == null) continue;
            user.updateRecord(new ServerInfo(entry.getKey(), isNetworkServer(entry.getKey())), remoteUser);
        }

        CommonLogger.getImplementation().debug("Updated global user " + user.getUniqueID());
        return user;
    }

    public void unloadGlobalUser(UUID uniqueID) {
        this.loadedUsers.remove(uniqueID);
    }

    private boolean isNetworkServer(String serverName) {
        return this.remoteStorages.containsKey(serverName) && this.remoteStorages.get(serverName).isNetworkServer();
    }

    public Map<String, MySQLStorage> getRemoteStorages() {
        return Collections.unmodifiableMap(this.remoteStorages);
    }
}