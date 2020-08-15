package space.devport.wertik.playtime.bungee.listeners;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import space.devport.wertik.playtime.bungee.BungeePlayTimePlugin;

/**
 * Look for join/leave and calculate spent time, then add to played time for this proxy.
 */
@RequiredArgsConstructor
public class BungeePlayerListener implements Listener {

    private final BungeePlayTimePlugin plugin;

    @EventHandler
    public void onJoin(LoginEvent event) {

    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {

    }
}