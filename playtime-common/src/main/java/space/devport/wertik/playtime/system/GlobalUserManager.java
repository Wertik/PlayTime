package space.devport.wertik.playtime.system;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.devport.wertik.playtime.console.CommonLogger;
import space.devport.wertik.playtime.mysql.ConnectionManager;
import space.devport.wertik.playtime.mysql.struct.ConnectionInfo;
import space.devport.wertik.playtime.mysql.struct.ServerConnection;
import space.devport.wertik.playtime.storage.mysql.MySQLStorage;
import space.devport.wertik.playtime.struct.GlobalUser;
import space.devport.wertik.playtime.struct.ServerInfo;
import space.devport.wertik.playtime.struct.User;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class GlobalUserManager {

    private final LoadCache<UUID, Void> loadCache = new LoadCache<>();

    private final Map<UUID, GlobalUser> loadedUsers = new HashMap<>();

    private final Map<String, MySQLStorage> remoteStorages = new HashMap<>();

    @Getter
    private final Map<String, TopCache> topCache = new HashMap<>();

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

            TopCache topCache = new TopCache(serverName, this::getTop, 10);
            this.topCache.put(serverName, topCache);
        }
    }

    public void startTopUpdate(int interval) {
        for (TopCache topCache : this.topCache.values()) {
            topCache.startUpdate(interval);
        }
    }

    public void loadTop() {
        for (TopCache cache : this.topCache.values())
            cache.load();
    }

    @NotNull
    public GlobalUser getOrCreateGlobalUser(UUID uniqueID) {
        return this.loadedUsers.containsKey(uniqueID) ? this.loadedUsers.get(uniqueID) : createGlobalUser(uniqueID);
    }

    @NotNull
    public GlobalUser createGlobalUser(UUID uniqueID) {
        GlobalUser user = new GlobalUser(uniqueID);
        this.loadedUsers.put(uniqueID, user);
        return user;
    }

    /**
     * Get a GlobalUser if loaded.
     * If he's not loaded, start loading and complete returned future when done.
     */
    @NotNull
    public CompletableFuture<GlobalUser> getOrLoadGlobalUser(UUID uniqueID) {

        // Return immediately if he's loaded.
        if (isLoaded(uniqueID))
            return CompletableFuture.supplyAsync(() -> getGlobalUser(uniqueID));

        // Trigger a load and complete the supply future when done.
        CompletableFuture<GlobalUser> future = new CompletableFuture<>();
        loadGlobalUser(uniqueID).thenRunAsync(() -> future.complete(getGlobalUser(uniqueID)));
        return future;
    }

    @Nullable
    public GlobalUser getGlobalUser(UUID uniqueID) {
        return this.loadedUsers.get(uniqueID);
    }

    public GlobalUser getGlobalUser(String name) {
        return this.loadedUsers.values().stream()
                .filter(u -> u.getLastKnownName() != null && u.getLastKnownName().equals(name))
                .findAny().orElseGet(() -> loadGlobalUser(name));
    }

    public GlobalUser loadGlobalUser(String name) {

        if (checkEmpty())
            return null;

        GlobalUser user = null;

        for (Map.Entry<String, MySQLStorage> entry : remoteStorages.entrySet()) {
            User remoteUser = entry.getValue().loadUser(name).join();

            if (remoteUser == null) continue;

            if (user == null)
                user = new GlobalUser(remoteUser.getUniqueID());

            user.updateRecord(new ServerInfo(entry.getKey(), isNetworkServer(entry.getKey())), remoteUser);
        }

        if (user != null)
            CommonLogger.getImplementation().debug("Updated global user " + user.getUniqueID());
        else
            CommonLogger.getImplementation().debug("Could not load global user " + name);
        return user;
    }

    @Nullable
    public UUID mapUsername(String name) {
        return this.loadedUsers.values().stream()
                .filter(u -> u.getLastKnownName() != null && u.getLastKnownName().equals(name))
                .map(GlobalUser::getUniqueID)
                .findAny()
                .orElse(Optional.ofNullable(loadGlobalUser(name)).map(GlobalUser::getUniqueID).orElse(null));
    }

    /**
     * Update data, if not loaded, create new.
     * Called when a player joins, or the plugin is enabled and he's online.
     */
    @NotNull
    public CompletableFuture<Void> loadGlobalUser(UUID uniqueID) {

        if (checkEmpty())
            return new CompletableFuture<>();

        GlobalUser user = getOrCreateGlobalUser(uniqueID);

        if (loadCache.isLoading(uniqueID))
            return loadCache.getLoading(uniqueID);

        List<CompletableFuture<GlobalUser>> futures = new ArrayList<>();

        for (Map.Entry<String, MySQLStorage> entry : remoteStorages.entrySet()) {
            futures.add(entry.getValue().loadUser(uniqueID).thenApplyAsync(remoteUser -> {
                if (remoteUser != null)
                    user.updateRecord(new ServerInfo(entry.getKey(), isNetworkServer(entry.getKey())), remoteUser);
                return user;
            }));
        }

        CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApplyAsync(globalUser -> {
                    CommonLogger.getImplementation().debug("Loaded global user " + uniqueID);
                    return globalUser;
                });

        loadCache.setLoading(uniqueID, future);
        future.thenRunAsync(() -> loadCache.setLoaded(uniqueID));

        return future;
    }

    public CompletableFuture<List<User>> getTop(String serverName, int count) {

        MySQLStorage storage = getRemoteStorages().get(serverName);

        if (storage == null)
            return CompletableFuture.supplyAsync(LinkedList::new);

        return storage.getTop(count).thenApplyAsync((top) -> {

            // Update user from cache if he's loaded.
            for (User topUser : top) {
                if (!isLoaded(topUser.getUniqueID()))
                    continue;

                ServerInfo serverInfo = new ServerInfo(serverName, isNetworkServer(serverName));
                GlobalUser globalUser = getGlobalUser(topUser.getUniqueID());

                if (globalUser == null)
                    continue;

                if (topUser.getLastKnownName() == null)
                    topUser.setLastKnownName(globalUser.getLastKnownName());
                if (globalUser.getPlayedTime(serverInfo) > topUser.getPlayedTimeRaw())
                    topUser.setPlayedTime(globalUser.getPlayedTime(serverInfo));
            }

            top.sort(Comparator.comparingLong(User::getPlayedTime).reversed());
            return top;
        });
    }

    public void dumpAll() {
        this.loadedUsers.clear();
    }

    private boolean isLoaded(UUID uniqueID) {
        return this.loadedUsers.containsKey(uniqueID);
    }

    private boolean checkEmpty() {
        return remoteStorages.isEmpty();
    }

    public void unloadGlobalUser(UUID uniqueID) {
        this.loadedUsers.remove(uniqueID);
    }

    public boolean isNetworkServer(String serverName) {
        return this.remoteStorages.containsKey(serverName) && this.remoteStorages.get(serverName).isNetworkServer();
    }

    public Map<String, MySQLStorage> getRemoteStorages() {
        return Collections.unmodifiableMap(this.remoteStorages);
    }
}