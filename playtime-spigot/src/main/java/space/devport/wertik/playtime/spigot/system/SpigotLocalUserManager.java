package space.devport.wertik.playtime.spigot.system;

import org.bukkit.Bukkit;
import space.devport.wertik.playtime.spigot.PlayTimePlugin;
import space.devport.wertik.playtime.spigot.utils.StatisticUtil;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.struct.User;
import space.devport.wertik.playtime.system.LocalUserManager;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Override the base to import statistics.
 */
public class SpigotLocalUserManager extends LocalUserManager {

    private final PlayTimePlugin plugin;

    public SpigotLocalUserManager(PlayTimePlugin plugin, IUserStorage storage) {
        super(storage);
        this.plugin = plugin;
    }

    @Override
    public CompletableFuture<User> loadUser(UUID uniqueID) {
        return super.loadUser(uniqueID).thenApplyAsync(this::update);
    }

    @Override
    public CompletableFuture<User> loadUser(String name) {
        return super.loadUser(name).thenApplyAsync(this::update);
    }

    @Override
    public User createUser(UUID uniqueID) {
        User user = super.createUser(uniqueID);
        update(user);
        return user;
    }

    public User update(User user) {
        UUID uniqueID = user.getUniqueID();

        // Import from statistics if we should.
        if (plugin.getConfig().getBoolean("import-statistics", false)) {
            long statisticTime = StatisticUtil.getTimeFromStatistics(uniqueID);
            if (statisticTime > user.getPlayedTime()) {
                long old = user.getPlayedTime();
                user.setPlayedTime(statisticTime);
                plugin.getConsoleOutput().debug("Imported " + user.getPlayedTimeRaw() + " from statistics for " + user.toString() + "(old: " + old + ")");
            }
        }
        return user;
    }

    @Override
    public boolean checkOnline(UUID uniqueID) {
        return Bukkit.getOfflinePlayer(uniqueID).isOnline();
    }
}