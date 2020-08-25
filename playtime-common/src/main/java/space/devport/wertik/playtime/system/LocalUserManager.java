package space.devport.wertik.playtime.system;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import space.devport.wertik.playtime.console.AbstractConsoleOutput;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.struct.User;

import java.util.*;
import java.util.stream.Collectors;

public class LocalUserManager {

    private final Map<UUID, User> localUsers = new HashMap<>();

    @Getter
    private final IUserStorage storage;

    public LocalUserManager(IUserStorage storage) {
        this.storage = storage;
    }

    public void loadAll(Set<UUID> players) {
        for (UUID uniqueID : players) {
            loadUser(uniqueID);
        }
    }

    public void saveAll() {
        for (User user : this.localUsers.values()) {
            storage.saveUser(user);
        }
    }

    /**
     * Save user to storage.
     */
    public boolean saveUser(UUID uniqueID) {
        User user = this.localUsers.getOrDefault(uniqueID, null);
        if (user == null) return false;
        storage.saveUser(user);
        AbstractConsoleOutput.getImplementation().debug("Saved user " + uniqueID);
        return true;
    }

    /**
     * Delete a user.
     */
    public void deleteUser(UUID uniqueID) {
        User user = this.localUsers.getOrDefault(uniqueID, null);
        if (user == null) return;
        this.localUsers.remove(uniqueID);
        this.storage.deleteUser(user);
        AbstractConsoleOutput.getImplementation().debug("Deleted user " + uniqueID);
    }

    /**
     * Save and unload from cache.
     */
    public void unloadUser(UUID uniqueID) {
        if (saveUser(uniqueID)) {
            this.localUsers.remove(uniqueID);
            AbstractConsoleOutput.getImplementation().debug("Unloaded user " + uniqueID);
        }
    }

    /**
     * Create a User, cache him.
     */
    public User createUser(UUID uniqueID) {
        User user = new User(uniqueID);
        user.setOnline(checkOnline(uniqueID));
        this.localUsers.put(uniqueID, user);
        AbstractConsoleOutput.getImplementation().debug("Created user " + uniqueID);
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
        AbstractConsoleOutput.getImplementation().debug("Users: " + this.localUsers.values().stream().map(u -> u.getUniqueID().toString()).collect(Collectors.joining(", ")));
        if (!this.localUsers.containsKey(uniqueID))
            return loadUser(uniqueID);
        return this.localUsers.getOrDefault(uniqueID, null);
    }

    /**
     * Load a User from storage and cache.
     */
    public User loadUser(UUID uniqueID) {
        User user = storage.loadUser(uniqueID);

        if (user != null) {
            user.setOnline(checkOnline(uniqueID));
            this.localUsers.put(uniqueID, user);
            AbstractConsoleOutput.getImplementation().debug("Loaded user " + uniqueID);
        }
        return user;
    }

    public boolean checkOnline(UUID uniqueID) {
        return false;
    }

    public boolean isLoaded(UUID uniqueID) {
        return this.localUsers.containsKey(uniqueID);
    }

    public Map<UUID, User> getLocalUsers() {
        return Collections.unmodifiableMap(this.localUsers);
    }
}