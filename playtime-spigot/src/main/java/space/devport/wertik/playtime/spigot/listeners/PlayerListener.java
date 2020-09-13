package space.devport.wertik.playtime.spigot.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import space.devport.wertik.playtime.spigot.PlayTimePlugin;
import space.devport.wertik.playtime.struct.User;

/**
 * Handles join/leave on a Spigot server.
 */
@RequiredArgsConstructor
public class PlayerListener implements Listener {

    private final PlayTimePlugin plugin;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        User user;
        if (!plugin.getLocalUserManager().isLoaded(player.getUniqueId())) {
            user = plugin.getLocalUserManager().loadUser(player.getUniqueId());

            // Cannot even load the user, create a new one.
            if (user == null)
                user = plugin.getLocalUserManager().createUser(player.getUniqueId());
        } else
            user = plugin.getLocalUserManager().getOrCreateUser(player.getUniqueId());

        // Set player online here, configure join time.
        user.setOnline();

        // Update global
        plugin.getGlobalUserManager().updateGlobalUser(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Unload global
        plugin.getGlobalUserManager().unloadGlobalUser(player.getUniqueId());

        // Unload the user
        if (plugin.getLocalUserManager().isLoaded(player.getUniqueId())) {
            User user = plugin.getLocalUserManager().getUser(player.getUniqueId());

            // Should never happen
            if (user == null) return;

            user.setOffline();
            plugin.getLocalUserManager().unloadUser(player.getUniqueId());
        }
    }
}