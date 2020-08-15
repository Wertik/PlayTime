package me.glaremasters.playertime.events;

import me.glaremasters.playertime.PlayerTime;
import me.glaremasters.playertime.utils.TimeUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Created by GlareMasters on 4/28/2018.
 */
public class Leave implements Listener {

    private final PlayerTime playerTime = PlayerTime.getInstance();

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (playerTime.getDatabase().hasTime(player.getUniqueId())) {
            playerTime.getDatabase().setTime(player.getUniqueId(), String.valueOf(TimeUtil.getTimeFromStatistics(player)));
        } else {
            playerTime.getDatabase().insertUser(player.getUniqueId(), String.valueOf(TimeUtil.getTimeFromStatistics(player)));
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        if (playerTime.getDatabase().hasTime(player.getUniqueId())) {
            playerTime.getDatabase().setTime(player.getUniqueId(), String.valueOf(TimeUtil.getTimeFromStatistics(player)));
        } else {
            playerTime.getDatabase().insertUser(player.getUniqueId(), String.valueOf(TimeUtil.getTimeFromStatistics(player)));
        }
    }
}