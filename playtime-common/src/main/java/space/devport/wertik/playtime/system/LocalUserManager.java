package space.devport.wertik.playtime.system;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.devport.wertik.playtime.console.CommonLogger;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.struct.User;
import space.devport.wertik.playtime.utils.CommonUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LocalUserManager {

    private final Map<UUID, User> loadedUsers = new HashMap<>();

    private final Set<UUID> loading = new HashSet<>();

    public void setLoading(UUID uniqueID) {
        this.loading.add(uniqueID);
    }

    public void setLoaded(UUID uniqueID) {
        this.loading.remove(uniqueID);
    }

    public boolean isLoading(UUID uniqueID) {
        return this.loading.contains(uniqueID);
    }

    @Getter
    private final IUserStorage storage;

    @Getter
    private final TopCache topCache;

    public LocalUserManager(IUserStorage storage) {
        this.storage = storage;
        DataManager.getInstance().setLocalUserManager(this);
        this.topCache = new TopCache((server, position) -> getTop(position), 10);
    }

    public void loadTop() {
        topCache.load(null);
    }

    public void loadOnline() {
        loadAll(CommonUtility.getImplementation().getOnlinePlayers());
    }

    public void loadAll(Set<UUID> players) {
        List<CompletableFuture<User>> futures = new ArrayList<>();
        for (UUID uniqueID : players) {
            futures.add(loadUser(uniqueID));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRunAsync(() -> CommonLogger.getImplementation().info("Loaded " + this.loadedUsers.size() + " user(s)..."));
    }

    public CompletableFuture<Void> saveAll() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (User user : this.loadedUsers.values()) {
            futures.add(storage.saveUser(user));
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRunAsync(() -> CommonLogger.getImplementation().info("Saved " + this.loadedUsers.size() + " user(s)..."));
    }

    public CompletableFuture<User> saveUser(UUID uniqueID) {
        return saveUser(getUser(uniqueID));
    }

    /**
     * Save user to storage.
     */
    public CompletableFuture<User> saveUser(User user) {
        return CompletableFuture.supplyAsync(() -> user).thenApplyAsync(savedUser -> {
            if (savedUser == null)
                return null;

            if (savedUser.getLastKnownName() == null)
                savedUser.setLastKnownName(CommonUtility.getImplementation().getPlayerName(savedUser.getUniqueID()));

            storage.saveUser(savedUser);
            CommonLogger.getImplementation().debug("Saved user " + savedUser.getUniqueID());
            return savedUser;
        });
    }

    /**
     * Delete a user.
     */
    public void deleteUser(UUID uniqueID) {
        User user = this.loadedUsers.getOrDefault(uniqueID, null);
        if (user == null) return;
        this.loadedUsers.remove(uniqueID);
        this.storage.deleteUser(user);
        CommonLogger.getImplementation().debug("Deleted user " + uniqueID);
    }

    /**
     * Save and unload from cache.
     */
    public CompletableFuture<Void> unloadUser(UUID uniqueID) {
        return CompletableFuture.supplyAsync(() -> {
            User user = this.loadedUsers.get(uniqueID);

            if (user == null)
                return null;

            user.setOffline();

            CommonLogger.getImplementation().debug("Unloaded user " + uniqueID);
            return user;
        }).thenAcceptAsync(this::saveUser);
    }

    /**
     * Create a User, cache him.
     */
    public User createUser(UUID uniqueID) {
        User user = new User(uniqueID);

        if (checkOnline(uniqueID)) {
            user.setOnline();
            user.setLastKnownName(CommonUtility.getImplementation().getPlayerName(uniqueID));
        }

        this.loadedUsers.put(uniqueID, user);
        this.storage.saveUser(user);
        CommonLogger.getImplementation().debug("Created user " + uniqueID);
        return user;
    }

    /**
     * Attempt to get user from cache.
     * If not there, attempt to load.
     * If not saved at all, create a new one.
     */
    @NotNull
    public CompletableFuture<User> getOrCreateUser(UUID uniqueID) {
        return getOrLoadUser(uniqueID).thenApplyAsync(user -> user == null ? createUser(uniqueID) : user);
    }

    public User getUser(UUID uniqueID) {
        return this.loadedUsers.get(uniqueID);
    }

    /**
     * Attempt to get user from cache.
     * If he's not loaded, attempt to do so.
     */
    @NotNull
    public CompletableFuture<User> getOrLoadUser(UUID uniqueID) {
        if (!this.loadedUsers.containsKey(uniqueID) && !isLoading(uniqueID))
            return loadUser(uniqueID);
        return CompletableFuture.supplyAsync(() -> this.loadedUsers.get(uniqueID));
    }

    @Nullable
    public User getUser(String name) {
        return this.loadedUsers.values().stream()
                .filter(u -> u.getLastKnownName() != null && u.getLastKnownName().equals(name))
                .findAny().orElse(null);
    }

    /**
     * Load a User from storage and cache him.
     */
    public CompletableFuture<User> loadUser(UUID uniqueID) {
        setLoading(uniqueID);
        return storage.loadUser(uniqueID).thenApplyAsync(user -> {

            if (user == null)
                return null;

            if (checkOnline(uniqueID))
                user.setOnline();

            this.loadedUsers.put(uniqueID, user);
            CommonLogger.getImplementation().debug("Loaded user " + uniqueID);
            setLoaded(uniqueID);
            return user;
        });
    }

    public CompletableFuture<User> loadUser(String name) {
        return storage.loadUser(name).thenApplyAsync(user -> {
            if (user != null) {
                UUID uniqueID = user.getUniqueID();

                if (checkOnline(uniqueID))
                    user.setOnline();

                this.loadedUsers.put(uniqueID, user);
                setLoaded(uniqueID);
                CommonLogger.getImplementation().debug("Loaded user " + uniqueID);
            } else {
                // Attempt to map username to UUID using remotes.
                UUID uniqueID = DataManager.getInstance().getGlobalUserManager().mapUsername(name);

                if (uniqueID == null)
                    return null;

                //TODO
                return loadUser(uniqueID).thenApplyAsync(uuidUser -> {
                    if (uuidUser == null)
                        uuidUser = createUser(uniqueID);

                    uuidUser.setLastKnownName(name);
                    return uuidUser;
                }).join();
            }
            return user;
        });
    }

    public CompletableFuture<List<User>> getTop(int count) {
        return storage.getTop(count).thenApplyAsync((top) -> {

            // Update user from cache if he's loaded.
            for (User topUser : top) {
                if (!isLoaded(topUser.getUniqueID()) || !checkOnline(topUser.getUniqueID()))
                    continue;

                User localUser = getUser(topUser.getUniqueID());

                if (localUser == null)
                    continue;

                if (topUser.getLastKnownName() == null)
                    topUser.setLastKnownName(localUser.getLastKnownName());
                if (localUser.getPlayedTime() > topUser.getPlayedTimeRaw())
                    topUser.setPlayedTime(localUser.getPlayedTime());
            }

            top.sort(Comparator.comparingLong(User::getPlayedTime).reversed());
            return top;
        });
    }

    public boolean checkOnline(UUID uniqueID) {
        return CommonUtility.getImplementation().isOnline(uniqueID);
    }

    public boolean isLoaded(UUID uniqueID) {
        return this.loadedUsers.containsKey(uniqueID);
    }

    public Map<UUID, User> getLoadedUsers() {
        return Collections.unmodifiableMap(this.loadedUsers);
    }
}