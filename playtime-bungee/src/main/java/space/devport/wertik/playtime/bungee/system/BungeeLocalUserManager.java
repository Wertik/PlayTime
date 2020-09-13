package space.devport.wertik.playtime.bungee.system;

import space.devport.wertik.playtime.bungee.BungeePlayTimePlugin;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.struct.GlobalUser;
import space.devport.wertik.playtime.struct.User;
import space.devport.wertik.playtime.system.DataManager;
import space.devport.wertik.playtime.system.LocalUserManager;

import java.util.UUID;

/**
 * Override to import times from servers.
 */
public class BungeeLocalUserManager extends LocalUserManager {

    private final BungeePlayTimePlugin plugin;

    public BungeeLocalUserManager(BungeePlayTimePlugin plugin, IUserStorage storage) {
        super(storage);
        this.plugin = plugin;
    }

    @Override
    public User createUser(UUID uniqueID) {
        User user = super.createUser(uniqueID);

        // Import server-wide times
        GlobalUser globalUser = DataManager.getInstance().getGlobalUserManager().getGlobalUser(uniqueID);
        long total = globalUser.totalTime();

        if (user.getPlayedTime() < total)
            user.setPlayedTime(total);

        return user;
    }
}