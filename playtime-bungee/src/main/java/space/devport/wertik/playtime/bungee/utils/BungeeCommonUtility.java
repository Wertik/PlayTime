package space.devport.wertik.playtime.bungee.utils;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import space.devport.wertik.playtime.bungee.BungeePlayTimePlugin;
import space.devport.wertik.playtime.utils.CommonUtility;

import java.util.UUID;

public class BungeeCommonUtility extends CommonUtility {

    private final BungeePlayTimePlugin plugin;

    public BungeeCommonUtility(BungeePlayTimePlugin plugin) {
        this.plugin = plugin;
        CommonUtility.setImplementation(this);
    }

    // Try to at least get it if he's online.
    @Override
    public String getOfflinePlayerName(UUID uniqueID) {
        ProxiedPlayer player = plugin.getProxy().getPlayer(uniqueID);
        return player == null ? null : player.getName();
    }

    @Override
    public boolean isOnline(UUID uniqueID) {
        ProxiedPlayer player = plugin.getProxy().getPlayer(uniqueID);
        return player != null && player.isConnected();
    }
}