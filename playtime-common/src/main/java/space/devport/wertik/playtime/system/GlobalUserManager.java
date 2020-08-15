package space.devport.wertik.playtime.system;

import lombok.Getter;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.struct.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GlobalUserManager {

    private final Map<UUID, User> globalUsers = new HashMap<>();

    @Getter
    private final IUserStorage storage;

    public GlobalUserManager() {
        this.storage = null;
        //TODO
    }

    public void load(UUID uniqueID) {

    }

    public void unload(UUID uniqueID) {

    }

    public void save(UUID uniqueID) {

    }
}