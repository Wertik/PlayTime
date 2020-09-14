package space.devport.wertik.playtime.system;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.devport.wertik.playtime.console.CommonLogger;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.struct.User;
import space.devport.wertik.playtime.utils.CommonUtility;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class LocalUserManager {

    private final Map<UUID, User> loadedUsers = new HashMap<>();

    @Getter
    private final IUserStorage storage;

    public LocalUserManager(IUserStorage storage) {
        this.storage = storage;
        DataManager.getInstance().setLocalUserManager(this);
    }

    public void loadOnline() {
        loadAll(CommonUtility.getImplementation().getOnlinePlayers());
    }

    public void loadAll(Set<UUID> players) {
        for (UUID uniqueID : players) {
            loadUser(uniqueID);
        }
        CommonLogger.getImplementation().info("Loaded " + this.loadedUsers.size() + " user(s)...");
    }

    public void saveAll() {
        for (User user : this.loadedUsers.values()) {
            storage.saveUser(user);
        }
    }

    /**
     * Save user to storage.
     */
    public boolean saveUser(UUID uniqueID) {
        User user = this.loadedUsers.getOrDefault(uniqueID, null);
        if (user == null) return false;

        // Attempt to fill out missing names.
        if (user.getLastKnownName() == null) {
            user.setLastKnownName(CommonUtility.getImplementation().getPlayerName(uniqueID));
        }

        storage.saveUser(user);
        CommonLogger.getImplementation().debug("Saved user " + uniqueID);
        return true;
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
    public void unloadUser(UUID uniqueID) {
        if (saveUser(uniqueID)) {
            this.loadedUsers.remove(uniqueID);
            CommonLogger.getImplementation().debug("Unloaded user " + uniqueID);
        }
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
    public User getOrCreateUser(UUID uniqueID) {
        User user = getUser(uniqueID);
        return user != null ? user : createUser(uniqueID);
    }

    /**
     * Attempt to get user from cache.
     * If he's not loaded, attempt to do so.
     */
    @Nullable
    public User getUser(UUID uniqueID) {
        if (!this.loadedUsers.containsKey(uniqueID))
            return loadUser(uniqueID);
        return this.loadedUsers.getOrDefault(uniqueID, null);
    }

    public User getUser(String name) {
        return this.loadedUsers.values().stream()
                .filter(u -> u.getLastKnownName() != null && u.getLastKnownName().equals(name))
                .findAny()
                .orElseGet(() -> loadUser(name));
    }

    /**
     * Load a User from storage and cache him.
     */
    public User loadUser(UUID uniqueID) {
        User user = storage.loadUser(uniqueID).join();

        if (user != null) {
            if (checkOnline(uniqueID)) user.setOnline();

            this.loadedUsers.put(uniqueID, user);
            CommonLogger.getImplementation().debug("Loaded user " + uniqueID);
        }
        return user;
    }

    @Nullable
    public User loadUser(String name) {
        User user = storage.loadUser(name).join();

        if (user != null) {
            UUID uniqueID = user.getUniqueID();

            if (checkOnline(uniqueID))
                user.setOnline();

            this.loadedUsers.put(uniqueID, user);
            CommonLogger.getImplementation().debug("Loaded user " + uniqueID);
        } else {
            // Attempt to map username to UUID using remotes.
            UUID uniqueID = DataManager.getInstance().getGlobalUserManager().mapUsername(name);
            if (uniqueID != null) {
                user = loadUser(uniqueID);
                user.setLastKnownName(name);
                return user;
            }
        }
        return user;
    }

    public CompletableFuture<List<User>> getTop(int count) {
        return storage.getTop(count).thenApplyAsync((top) -> {

            CommonLogger.getImplementation().debug("Top users from db: " + top.stream().map(User::getLastKnownName).collect(Collectors.joining(", ")));

            // Update user from cache if he's loaded.
            for (User topUser : top) {
                if (!isLoaded(topUser.getUniqueID()) || !checkOnline(topUser.getUniqueID()))
                    continue;

                User localUser = getUser(topUser.getUniqueID());

                if (localUser == null) continue;

                if (topUser.getLastKnownName() == null)
                    topUser.setLastKnownName(localUser.getLastKnownName());
                if (localUser.getPlayedTime() > topUser.getPlayedTimeRaw())
                    topUser.setPlayedTime(localUser.getPlayedTime());

                CommonLogger.getImplementation().debug("Updated user " + topUser.getLastKnownName() + " from cache.");
            }

            // Re-sort
            return top.stream()
                    .sorted(Comparator.comparingLong(User::getPlayedTime).reversed())
                    .collect(Collectors.toList());
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