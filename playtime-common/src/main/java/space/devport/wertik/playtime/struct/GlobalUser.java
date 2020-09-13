package space.devport.wertik.playtime.struct;

import lombok.Getter;
import space.devport.wertik.playtime.system.DataManager;

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

    private final Map<ServerInfo, User> userRecord = new HashMap<>();

    public GlobalUser(UUID uniqueID) {
        this.uniqueID = uniqueID;
    }

    public long getPlayedTime(ServerInfo serverInfo) {
        long time = this.userRecord.containsKey(serverInfo) ? this.userRecord.get(serverInfo).getPlayedTimeRaw() : 0;
        if (serverInfo.isNetworkWide())
            time += DataManager.getInstance().getLocalUserManager().getOrCreateUser(uniqueID).sinceJoin();
        return time;
    }

    public void updateRecord(ServerInfo info, User user) {
        this.userRecord.put(info, user);
    }

    public void removeUserRecord(ServerInfo serverInfo) {
        this.userRecord.remove(serverInfo);
    }

    public Map<ServerInfo, User> getUserRecord() {
        return Collections.unmodifiableMap(userRecord);
    }
}