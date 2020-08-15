package space.devport.wertik.playtime.spigot.system;

import space.devport.wertik.playtime.spigot.PlayTimePlugin;
import space.devport.wertik.playtime.spigot.utils.StatisticUtil;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.struct.User;
import space.devport.wertik.playtime.system.LocalUserManager;

import java.util.UUID;

public class SpigotLocalUserManager extends LocalUserManager {

    private final PlayTimePlugin plugin;

    public SpigotLocalUserManager(PlayTimePlugin plugin, IUserStorage storage) {
        super(storage);
        this.plugin = plugin;
    }

    @Override
    public User createUser(UUID uniqueID) {
        User user = super.createUser(uniqueID);

        // Import from statistics if we should.
        if (plugin.getConfig().getBoolean("import-statistics", false)) {
            user.setPlayedTime(StatisticUtil.getTimeFromStatistics(uniqueID));
            plugin.getConsoleOutput().debug("Imported " + user.getPlayedTimeRaw() + " played time from statistics for " + uniqueID);
        }

        return user;
    }
}