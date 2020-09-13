package space.devport.wertik.playtime.struct;

import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Holds played time on other servers.
 */
public class GlobalUser {

    @Getter
    private final UUID uniqueID;

    private final Map<String, User> userRecord = new HashMap<>();

    public GlobalUser(UUID uniqueID) {
        this.uniqueID = uniqueID;
    }

    public long getPlayedTime(String serverName) {
        return this.userRecord.containsKey(serverName) ? this.userRecord.get(serverName).getPlayedTimeRaw() : 0;
    }

    public void updateRecord(String serverName, User user) {
        this.userRecord.put(serverName, user);
    }

    public void removeUserRecord(String serverName) {
        this.userRecord.remove(serverName);
    }

    public Map<String, User> getUserRecord() {
        return Collections.unmodifiableMap(userRecord);
    }
}