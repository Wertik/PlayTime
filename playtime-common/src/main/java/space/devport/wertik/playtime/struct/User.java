package space.devport.wertik.playtime.struct;

import lombok.Getter;

import java.util.UUID;

/**
 * Holds played time on local server.
 */
public class User {

    @Getter
    private final UUID uniqueID;

    @Getter
    private long playedTime;

    public User(UUID uniqueID) {
        this.uniqueID = uniqueID;
    }

    public User(UUID uniqueID, long playedTime) {
        this.uniqueID = uniqueID;
        this.playedTime = playedTime;
    }
}