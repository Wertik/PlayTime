package space.devport.wertik.playtime.storage.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.struct.User;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class JsonStorage implements IUserStorage {

    private final Gson gson = new GsonBuilder()
            //.setPrettyPrinting()
            .create();

    @Override
    public void initialize() {

    }

    @Override
    public User loadUser(UUID uniqueID) {
        return null;
    }

    @Override
    public void saveUser(User user) {

    }

    @Override
    public void deleteUser(User user) {

    }

    @Override
    public void purge() {

    }

    @Override
    public void purge(Function<User, Boolean> conditions) {

    }

    @Override
    public Set<User> loadAll() {
        return null;
    }

    @Override
    public void saveAll(Set<User> users) {

    }
}
