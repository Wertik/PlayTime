package space.devport.wertik.playtime.utils;

import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class CommonUtility {

    @Setter
    private static CommonUtility implementation;

    public static CommonUtility getImplementation() {
        if (implementation == null)
            setImplementation(new DummyCommonUtility());
        return implementation;
    }

    @Nullable
    public abstract String getOfflinePlayerName(UUID uniqueID);

    public abstract boolean isOnline(UUID uniqueID);
}