package space.devport.wertik.playtime.storage;

import space.devport.wertik.playtime.struct.User;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public interface IUserStorage {

    void initialize();

    User loadUser(UUID uniqueID);

    User loadUser(String name);

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
    void purge(Function<User, Boolean> conditions);
}