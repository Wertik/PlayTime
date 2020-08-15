package space.devport.wertik.playtime.system;

import lombok.Getter;
import space.devport.wertik.playtime.console.AbstractConsoleOutput;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.struct.User;

import java.util.*;

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
        this.localUsers.put(uniqueID, user);
        AbstractConsoleOutput.getImplementation().debug("Created new User " + uniqueID);
        return user;
    }

    /**
     * Get User from cache.
     */
    public User getUser(UUID uniqueID) {
        return this.localUsers.getOrDefault(uniqueID, loadUser(uniqueID));
    }

    /**
     * Load a User from storage and cache.
     */
    public User loadUser(UUID uniqueID) {
        User user = storage.loadUser(uniqueID);
        if (user != null) {
            this.localUsers.put(uniqueID, user);
            AbstractConsoleOutput.getImplementation().debug("Loaded user " + uniqueID);
        }
        return user;
    }

    public boolean isLoaded(UUID uniqueID) {
        return this.localUsers.containsKey(uniqueID);
    }

    public Map<UUID, User> getLocalUsers() {
        return Collections.unmodifiableMap(this.localUsers);
    }
}