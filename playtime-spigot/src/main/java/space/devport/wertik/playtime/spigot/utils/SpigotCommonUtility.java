package space.devport.wertik.playtime.spigot.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import space.devport.wertik.playtime.utils.CommonUtility;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpigotCommonUtility extends CommonUtility {

    @Override
    public String getPlayerName(UUID uniqueID) {
        return Bukkit.getOfflinePlayer(uniqueID).getName();
    }

    @Override
    public boolean isOnline(UUID uniqueID) {
        return Bukkit.getOfflinePlayer(uniqueID).isOnline();
    }

    @Override
    public @NotNull Set<UUID> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toSet());
    }
}
