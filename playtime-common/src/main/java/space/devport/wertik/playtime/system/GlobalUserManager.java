package space.devport.wertik.playtime.system;

import lombok.Getter;
import lombok.Setter;
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
import java.util.concurrent.atomic.AtomicReference;

public class GlobalUserManager {

    private final LoadCache<UUID, Void> loadCache = new LoadCache<>();
    private final LoadCache<String, Void> nameLoadCache = new LoadCache<>();

    private final Map<UUID, GlobalUser> loadedUsers = new HashMap<>();

    private final Map<ServerInfo, MySQLStorage> remoteStorages = new HashMap<>();

    @Getter
    private final Map<ServerInfo, TopCache> topCache = new HashMap<>();

    @Getter
    @Setter
    private boolean nickReattempt = false;

    public GlobalUserManager() {
        DataManager.getInstance().setGlobalUserManager(this);
    }

    @NotNull
    public ServerInfo getServerInfo(String server) {
        return remoteStorages.keySet().stream()
                .filter(i -> i.getName().equals(server))
                .findAny().orElse(new ServerInfo(server));
    }

    /**
     * @param tableName  of the table to use
     * @param serverName Name of the server and the table to connect to.
     */
    public void initializeStorage(String serverName, ConnectionInfo connectionInfo, String tableName, boolean... networkServer) {

        ServerInfo info = new ServerInfo(serverName, networkServer.length > 0 && networkServer[0]);

        if (this.remoteStorages.containsKey(info))
            return;

        ServerConnection connection = ConnectionManager.getInstance().initializeConnection(serverName, connectionInfo);

        if (connection == null) {
            CommonLogger.getImplementation().err("Could not initialize remote storage " + serverName + " with table " + tableName);
        } else {
            MySQLStorage storage = new MySQLStorage(connection, tableName, info.isNetworkWide());

            this.remoteStorages.put(info, storage);
            CommonLogger.getImplementation().info("Initialized remote storage for server " + info.toString() + " with table " + tableName);

            if (info.isNetworkWide())
                CommonLogger.getImplementation().info("Using it as a network server.");

            TopCache topCache = new TopCache(serverName, this::getTop, 10);
            this.topCache.put(info, topCache);
        }
    }

    public void startTopUpdate(int interval) {
        for (TopCache topCache : topCache.values()) {
            topCache.startUpdate(interval);
        }
    }

    public void stopTopCache() {
        topCache.values().forEach(TopCache::stop);
    }

    public void loadTop() {
        for (TopCache cache : topCache.values())
            cache.load();
    }

    @NotNull
    public GlobalUser getOrCreateGlobalUser(UUID uniqueID) {
        return loadedUsers.containsKey(uniqueID) ? loadedUsers.get(uniqueID) : createGlobalUser(uniqueID);
    }

    @NotNull
    public GlobalUser createGlobalUser(UUID uniqueID) {
        GlobalUser user = new GlobalUser(uniqueID);
        this.loadedUsers.put(uniqueID, user);

        // Attempt to update his local username.
        User localUser = DataManager.getInstance().getLocalUserManager().getUser(uniqueID);
        if (localUser != null && localUser.getLastKnownName() != null) {
            user.setLastKnownName(localUser.getLastKnownName());
        }

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

    @Nullable
    public UUID mapUsername(@NotNull String name) {
        GlobalUser user = getGlobalUser(name);
        return user == null ? null : user.getUniqueID();
    }

    @Nullable
    public GlobalUser getGlobalUser(@NotNull String name) {
        Objects.requireNonNull(name, "Cannot query by null name.");

        return this.loadedUsers.values().stream()
                .filter(u -> name.equals(u.getLastKnownName()))
                .findAny().orElse(null);
    }

    public CompletableFuture<Void> loadGlobalUser(@NotNull String name) {
        Objects.requireNonNull(name, "Cannot load a user by null name.");

        if (checkEmpty())
            return CompletableFuture.completedFuture(null);

        if (nameLoadCache.isLoading(name))
            return nameLoadCache.getLoading(name);

        CompletableFuture<Void> future = new CompletableFuture<>();
        nameLoadCache.setLoading(name, future);

        final AtomicReference<GlobalUser> user = new AtomicReference<>();

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Map.Entry<ServerInfo, MySQLStorage> entry : remoteStorages.entrySet()) {
            CompletableFuture<Void> userFuture = entry.getValue().loadUser(name).thenAcceptAsync(remoteUser -> {
                if (remoteUser == null)
                    return;

                if (user.get() == null)
                    user.set(new GlobalUser(remoteUser.getUniqueID()));

                user.get().updateRecord(entry.getKey(), remoteUser);
                CommonLogger.getImplementation().debug("Queried time for " + user.toString() + " from " + entry.getKey().toString());
            });

            userFuture.exceptionally(e -> {
                CommonLogger.getImplementation().warn("Could not load remote " + entry.getKey().toString() + " for user " + user.get().toString());
                e.printStackTrace();
                return null;
            });

            futures.add(userFuture);
        }

        future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        return future;
    }

    /**
     * Update data, if not loaded, create new.
     * Called when a player joins, or the plugin is enabled and he's online.
     */
    @NotNull
    public CompletableFuture<Void> loadGlobalUser(UUID uniqueID) {

        if (checkEmpty())
            return CompletableFuture.completedFuture(null);

        if (loadCache.isLoading(uniqueID))
            return loadCache.getLoading(uniqueID);

        CompletableFuture<Void> future = new CompletableFuture<>();
        loadCache.setLoading(uniqueID, future);

        GlobalUser user = createGlobalUser(uniqueID);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Map.Entry<ServerInfo, MySQLStorage> entry : remoteStorages.entrySet()) {
            CompletableFuture<Void> userFuture = entry.getValue().loadUser(uniqueID).thenAcceptAsync(remoteUser -> {
                if (remoteUser != null) {
                    user.updateRecord(entry.getKey(), remoteUser);
                    CommonLogger.getImplementation().debug("Queried time (" + remoteUser.getPlayedTime() + ") for " + user.toString() + " from " + entry.getKey().toString());
                    return;
                }
                CommonLogger.getImplementation().debug("Queried time for " + user.toString() + " from " + entry.getKey().toString() + ", doesn't have an account there.");

                if (nickReattempt && user.getLastKnownName() != null) {
                    loadGlobalUser(user.getLastKnownName());
                    CommonLogger.getImplementation().debug("Reattempting with his username " + user.getLastKnownName());
                }
            });

            userFuture.exceptionally(e -> {
                CommonLogger.getImplementation().warn("Could not load remote " + entry.getKey().toString() + " for global user " + user.getLastKnownName());
                e.printStackTrace();
                return null;
            });

            futures.add(userFuture);
        }

        future = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        future.exceptionally(e -> {
            CommonLogger.getImplementation().warn("Could not load global user " + user.toString() + " properly.");
            e.printStackTrace();
            return null;
        });

        future.thenRunAsync(() -> {
            loadCache.setLoaded(uniqueID);
            CommonLogger.getImplementation().debug("Loaded global user " + user.toString());
        });

        return future;
    }

    public CompletableFuture<List<User>> getTop(String serverName, int count) {

        MySQLStorage storage = getRemote(serverName);

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
        return getServerInfo(serverName).isNetworkWide();
    }

    @Nullable
    public MySQLStorage getRemote(String name) {
        return this.remoteStorages.keySet().stream()
                .filter(i -> i.getName().equals(name))
                .findAny()
                .map(remoteStorages::get)
                .orElse(null);
    }

    public boolean hasRemote(String name) {
        return this.remoteStorages.keySet().stream().anyMatch(i -> i.getName().equals(name));
    }

    public Map<ServerInfo, MySQLStorage> getRemoteStorages() {
        return Collections.unmodifiableMap(this.remoteStorages);
    }
}