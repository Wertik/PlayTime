package space.devport.wertik.playtime;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public abstract class CommonUtility {

    @Getter
    @Setter
    private static CommonUtility implementation;

    public CommonUtility() {
    }

    public abstract String getOfflinePlayerName(UUID uniqueID);
}