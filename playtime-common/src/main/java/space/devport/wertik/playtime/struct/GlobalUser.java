package space.devport.wertik.playtime.struct;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Holds played time on other servers.
 */
public class GlobalUser {

    @Getter
    private final UUID uniqueID;

    private final Map<String, Long> playedTime = new HashMap<>();

    public GlobalUser(UUID uniqueID) {
        this.uniqueID = uniqueID;
    }
}