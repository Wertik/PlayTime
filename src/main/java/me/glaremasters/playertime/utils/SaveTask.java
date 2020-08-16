package me.glaremasters.playertime.utils;

import me.glaremasters.playertime.PlayerTime;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import static me.glaremasters.playertime.commands.CMDCheck.ticksToMillis;

/**
 * Created by GlareMasters
 * Date: 7/25/2018
 * Time: 2:45 PM
 */
public class SaveTask {

    public static void startTask() {
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(PlayerTime.getInstance(), () -> {
            if (Bukkit.getOnlinePlayers().size() > 0) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (PlayerTime.getInstance().getDatabase().hasTime(player.getUniqueId().toString())) {
                        PlayerTime.getInstance().getDatabase().setTime(player.getUniqueId().toString(), String.valueOf(ticksToMillis(player)));
                    } else {
                        PlayerTime.getInstance().getDatabase().insertUser(player.getUniqueId().toString(),String.valueOf(ticksToMillis(player)));
                    }
                }
            }
        }, 1200L, 1200L);
    }
}