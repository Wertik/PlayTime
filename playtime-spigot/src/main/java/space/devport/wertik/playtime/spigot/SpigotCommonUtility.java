package space.devport.wertik.playtime.spigot;

import org.bukkit.Bukkit;
import space.devport.wertik.playtime.CommonUtility;

import java.util.UUID;

public class SpigotCommonUtility extends CommonUtility {

    @Override
    public String getOfflinePlayerName(UUID uniqueID) {
        return Bukkit.getOfflinePlayer(uniqueID).getName();
    }
}
