package space.devport.wertik.playtime.utils;

import java.util.UUID;

public class DummyCommonUtility extends CommonUtility {

    @Override
    public String getOfflinePlayerName(UUID uniqueID) {
        return null;
    }

    @Override
    public boolean isOnline(UUID uniqueID) {
        return false;
    }
}