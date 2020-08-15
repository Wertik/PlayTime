package space.devport.wertik.playtime.storage.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.struct.User;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class JsonStorage implements IUserStorage {

    private final Gson gson = new GsonBuilder()
            //.setPrettyPrinting()
            .create();

    @Override
    public void initialize() {
        throw new NotImplementedException();
    }

    @Override
    public User loadUser(UUID uniqueID) {
        throw new NotImplementedException();
    }

    @Override
    public void saveUser(User user) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteUser(User user) {
        throw new NotImplementedException();
    }

    @Override
    public void purge() {
        throw new NotImplementedException();
    }

    @Override
    public void purge(Function<User, Boolean> conditions) {
        throw new NotImplementedException();
    }

    @Override
    public Set<User> loadAll() {
        throw new NotImplementedException();
    }

    @Override
    public void saveAll(Set<User> users) {
        throw new NotImplementedException();
    }
}
