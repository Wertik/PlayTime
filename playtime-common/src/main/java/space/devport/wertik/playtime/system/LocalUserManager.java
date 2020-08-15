package space.devport.wertik.playtime.system;

import lombok.Getter;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.struct.User;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LocalUserManager {

    private final Map<UUID, User> localUsers = new HashMap<>();

    @Getter
    private final IUserStorage storage;

    /**
     * Local server name.
     */
    @Getter
    private final String serverName;

    public LocalUserManager(IUserStorage storage, String serverName) {
        this.storage = storage;
        this.serverName = serverName;
    }

    public void save() {

    }

    public User getUser() {
        return null;
    }
}