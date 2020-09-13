package space.devport.wertik.playtime.spigot.utils;

import org.bukkit.Bukkit;
import space.devport.wertik.playtime.utils.CommonUtility;

import java.util.UUID;

public class SpigotCommonUtility extends CommonUtility {

    @Override
    public String getOfflinePlayerName(UUID uniqueID) {
        return Bukkit.getOfflinePlayer(uniqueID).getName();
    }

    @Override
    public boolean isOnline(UUID uniqueID) {
        return Bukkit.getOfflinePlayer(uniqueID).isOnline();
    }
}
