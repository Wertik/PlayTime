package space.devport.wertik.playtime.bungee.listeners;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
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
        final UUID uniqueID = event.getConnection().getUniqueId();

        plugin.getLocalUserManager().loadUser(uniqueID).thenAccept(User::setOnline);
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        final UUID uniqueID = event.getPlayer().getUniqueId();

        plugin.getLocalUserManager().unloadUser(uniqueID);
    }

    // Save user data on server switch
    @EventHandler
    public void onSwitch(ServerSwitchEvent event) {
        final UUID uniqueID = event.getPlayer().getUniqueId();
        plugin.getLocalUserManager().saveUser(uniqueID);
    }
}