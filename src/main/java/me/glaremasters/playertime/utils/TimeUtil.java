package me.glaremasters.playertime.utils;

import org.bukkit.Statistic;
import org.bukkit.entity.Player;

public class TimeUtil {

    public static int getTimeFromStatistics(Player player) {
        int time;
        try {
            time = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        } catch (NoSuchFieldError e) {
            time = player.getStatistic(Statistic.valueOf("PLAY_ONE_TICK"));
        }

        int seconds = time / 20;
        return seconds * 1000;
    }
}