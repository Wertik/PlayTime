package space.devport.wertik.playtime.struct;

import lombok.Getter;
import lombok.Setter;
import space.devport.wertik.playtime.system.DataManager;
import space.devport.wertik.playtime.utils.CommonUtility;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds played time on other servers.
 */
public class GlobalUser {

    @Getter
    private final UUID uniqueID;

    @Getter
    @Setter
    private String lastKnownName;

    private final Map<ServerInfo, User> userRecord = new ConcurrentHashMap<>();

    public GlobalUser(UUID uniqueID) {
        this.uniqueID = uniqueID;
    }

    public long getPlayedTime(ServerInfo serverInfo) {
        long time = this.userRecord.containsKey(serverInfo) ? this.userRecord.get(serverInfo).getPlayedTimeRaw() : 0;
        if (serverInfo.isNetworkWide() && CommonUtility.getImplementation().isOnline(uniqueID)) {
            time += DataManager.getInstance().getLocalUserManager().getOrCreateUser(uniqueID).join().sinceJoin();
        }
        return time;
    }

    public long totalTime() {
        return this.userRecord.values().stream().mapToLong(User::getPlayedTimeRaw).sum();
    }

    public void updateRecord(ServerInfo info, User user) {
        // Update name
        if (lastKnownName == null && user.getLastKnownName() != null)
            this.lastKnownName = user.getLastKnownName();

        this.userRecord.put(info, user);
    }

    public void removeUserRecord(ServerInfo serverInfo) {
        this.userRecord.remove(serverInfo);
    }

    public Map<ServerInfo, User> getUserRecord() {
        return Collections.unmodifiableMap(userRecord);
    }

    @Override
    public String toString() {
        return (lastKnownName == null ? uniqueID.toString() : lastKnownName) + "[" + userRecord.size() + "]";
    }
}