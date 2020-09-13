package space.devport.wertik.playtime.bungee.system;

import space.devport.wertik.playtime.bungee.BungeePlayTimePlugin;
import space.devport.wertik.playtime.console.CommonLogger;
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
        update(user);
        return user;
    }

    @Override
    public User loadUser(UUID uniqueID) {
        User user = super.loadUser(uniqueID);
        if (user != null)
            update(user);
        return user;
    }

    /**
     * Check if time stored is above or same with sum of all connected servers.
     */
    private void update(User user) {
        if (!plugin.getConfiguration().getBoolean("import-connected-servers", false)) return;

        // Import server-wide times
        GlobalUser globalUser = DataManager.getInstance().getGlobalUserManager().getGlobalUser(user.getUniqueID());
        long total = globalUser.totalTime();

        if (user.getPlayedTime() < total) {
            user.setPlayedTime(total);
            CommonLogger.getImplementation().debug("Imported time from remote servers for user " + user.getUniqueID() + " = " + total);
        }
    }
}