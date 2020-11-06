package space.devport.wertik.playtime.struct;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.Setter;
import space.devport.wertik.playtime.console.CommonLogger;

import java.util.Objects;
import java.util.UUID;

/**
 * Holds played time on local server.
 */
public class User {

    @Getter
    private final UUID uniqueID;

    @Getter
    @Setter
    private String lastKnownName;

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
        updateJoinTime();
    }

    public void updateJoinTime() {
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

    public String getName() {
        return Strings.isNullOrEmpty(lastKnownName) ? "not-loaded" : lastKnownName;
    }

    @Override
    public String toString() {
        return getName() + "(" + uniqueID.toString() + ")[pt:" + playedTime + ";jt:" + joinTime + ";on:" + online + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(uniqueID, user.uniqueID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueID);
    }
}