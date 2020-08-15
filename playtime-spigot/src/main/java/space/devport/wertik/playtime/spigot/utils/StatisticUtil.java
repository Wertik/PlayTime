package space.devport.wertik.playtime.spigot.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;

import java.util.UUID;

@UtilityClass
public class StatisticUtil {

    /**
     * Get time played in milliseconds by player from Minecraft statistics.
     *
     * @param uniqueID Player UUID
     * @return Played time in millis
     */
    public long getTimeFromStatistics(UUID uniqueID) {
        return getTimeFromStatistics(Bukkit.getOfflinePlayer(uniqueID));
    }

    /**
     * Get time played in milliseconds by player from Minecraft statistics.
     *
     * @param player OfflinePlayer to look for
     * @return Played time in millis
     */
    public long getTimeFromStatistics(OfflinePlayer player) {

        long time;
        try {
            time = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        } catch (NoSuchFieldError e) {
            time = player.getStatistic(Statistic.valueOf("PLAY_ONE_TICK"));
        }

        return time * 50L;
    }
}