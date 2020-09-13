package space.devport.wertik.playtime.struct;

import lombok.Getter;
import lombok.Setter;
import space.devport.wertik.playtime.console.CommonLogger;

import java.util.UUID;

/**
 * Holds played time on local server.
 */
public class User {

    @Getter
    private final UUID uniqueID;

    @Getter
    @Setter
    private transient boolean online = false;

    @Getter
    private transient long joinTime;

    @Setter
    private long playedTime;

    public User(UUID uniqueID) {
        this.uniqueID = uniqueID;
    }

    public User(UUID uniqueID, long playedTime) {
        this.uniqueID = uniqueID;
        this.playedTime = playedTime;
    }

    public void updatePlayedTime() {
        this.playedTime += sinceJoin();
        CommonLogger.getImplementation().debug("Updated played time for user " + uniqueID + " to " + this.playedTime);
    }

    public long sinceJoin() {
        return System.currentTimeMillis() - joinTime;
    }

    public void setOnline() {
        this.online = true;
        this.joinTime = System.currentTimeMillis();
    }

    public void setOffline() {
        this.online = false;
        updatePlayedTime();
    }

    public long getPlayedTime() {
        return online ? sinceJoin() + playedTime : playedTime;
    }

    public long getPlayedTimeRaw() {
        return this.playedTime;
    }
}