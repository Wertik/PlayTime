package space.devport.wertik.playtime.storage.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import space.devport.wertik.playtime.NotImplementedException;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.struct.User;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class JsonStorage implements IUserStorage {

    private final Gson gson = new GsonBuilder()
            //.setPrettyPrinting()
            .create();

    @Override
    public void purge(Predicate<User> conditions) {
        throw new NotImplementedException();
    }

    @Override
    public void initialize() {
        throw new NotImplementedException();
    }

    @Override
    public CompletableFuture<User> loadUser(UUID uniqueID) {
        throw new NotImplementedException();
    }

    @Override
    public CompletableFuture<User> loadUser(String name) {
        throw new NotImplementedException();
    }

    @Override
    public CompletableFuture<Void> saveUser(User user) {
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
    public Set<User> loadAll() {
        throw new NotImplementedException();
    }

    @Override
    public CompletableFuture<List<User>> getTop(int count) {
        throw new NotImplementedException();
    }
}
