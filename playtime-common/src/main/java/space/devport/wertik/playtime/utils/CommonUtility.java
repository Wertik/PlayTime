package space.devport.wertik.playtime.utils;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
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
    public abstract String getPlayerName(UUID uniqueID);

    public abstract boolean isOnline(UUID uniqueID);

    @NotNull
    public abstract Set<UUID> getOnlinePlayers();
}