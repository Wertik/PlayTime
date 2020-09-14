package space.devport.wertik.playtime.storage;

import space.devport.wertik.playtime.struct.User;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public interface IUserStorage {

    void initialize();

    CompletableFuture<User> loadUser(UUID uniqueID);

    CompletableFuture<User> loadUser(String name);

    Set<User> loadAll();

    void saveUser(User user);

    void deleteUser(User user);

    /**
     * Purge all entries.
     */
    void purge();

    /**
     * Purge entries based on a condition.
     */
    void purge(Predicate<User> conditions);
}