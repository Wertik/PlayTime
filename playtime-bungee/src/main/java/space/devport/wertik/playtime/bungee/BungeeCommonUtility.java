package space.devport.wertik.playtime.bungee;

import space.devport.wertik.playtime.CommonUtility;

import java.util.UUID;

public class BungeeCommonUtility extends CommonUtility {

    //TODO There's no way to get an offline player on proxy, is there?
    @Override
    public String getOfflinePlayerName(UUID uniqueID) {
        return BungeePlayTimePlugin.getInstance().getProxy().getPlayer(uniqueID).getName();
    }
}