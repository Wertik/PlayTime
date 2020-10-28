package space.devport.wertik.playtime.spigot.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import space.devport.wertik.playtime.spigot.PlayTimePlugin;

/**
 * Handles join/leave on a Spigot server.
 */
@RequiredArgsConstructor
public class PlayerListener implements Listener {

    private final PlayTimePlugin plugin;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        plugin.getLocalUserManager().getOrCreateUser(player.getUniqueId()).thenApply(user -> {
            user.setOnline();
            return user;
        });

        plugin.getGlobalUserManager().loadGlobalUser(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        plugin.getGlobalUserManager().unloadGlobalUser(player.getUniqueId());
        plugin.getLocalUserManager().unloadUser(player.getUniqueId());
    }
}