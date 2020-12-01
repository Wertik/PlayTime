package space.devport.wertik.playtime.spigot.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import space.devport.utils.ParseUtil;
import space.devport.utils.utility.reflection.ServerVersion;

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
        if (ServerVersion.isCurrentAbove(ServerVersion.v1_13))
            time = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        else
            time = getTimeFromStatisticsLegacy(player);

        return time * 50L;
    }

    private long getTimeFromStatisticsLegacy(OfflinePlayer player) {
        Statistic statistic = ParseUtil.parseEnum("PLAY_ONE_TICK", Statistic.class);

        if (statistic == null)
            return 0;

        return player.getStatistic(statistic);
    }
}