package space.devport.wertik.playtime.bungee.listeners;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import space.devport.wertik.playtime.bungee.BungeePlayTimePlugin;
import space.devport.wertik.playtime.struct.User;

import java.util.UUID;

/**
 * Look for join/leave and calculate spent time, then add to played time for this proxy.
 */
@RequiredArgsConstructor
public class BungeePlayerListener implements Listener {

    private final BungeePlayTimePlugin plugin;

    @EventHandler
    public void onJoin(LoginEvent event) {

        UUID uniqueID = event.getConnection().getUniqueId();

        User user;
        if (!plugin.getLocalUserManager().isLoaded(uniqueID)) {
            user = plugin.getLocalUserManager().loadUser(uniqueID);

            // Cannot even load the user, create a new one.
            if (user == null)
                user = plugin.getLocalUserManager().createUser(uniqueID);
        } else
            user = plugin.getLocalUserManager().getOrCreateUser(uniqueID);

        // Set player online here, configure join time.
        user.setOnline();
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        UUID uniqueID = event.getPlayer().getUniqueId();

        // Unload the user
        if (plugin.getLocalUserManager().isLoaded(uniqueID)) {
            User user = plugin.getLocalUserManager().getUser(uniqueID);

            // Should never happen
            if (user == null) return;

            user.setOffline();
            plugin.getLocalUserManager().unloadUser(uniqueID);
        }
    }
}