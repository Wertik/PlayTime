package space.devport.wertik.playtime.utils;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
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

    @Override
    public @NotNull Set<UUID> getOnlinePlayers() {
        return new HashSet<>();
    }
}