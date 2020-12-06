package space.devport.wertik.playtime.struct;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import space.devport.wertik.playtime.system.DataManager;
import space.devport.wertik.playtime.utils.CommonUtility;

import java.util.*;

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
        if (serverInfo.isNetworkWide() && CommonUtility.getImplementation().isOnline(uniqueID)) {
            time += DataManager.getInstance().getLocalUserManager().getOrCreateUser(uniqueID).join().sinceJoin();
        }
        return time;
    }

    public long totalTime() {
        return this.userRecord.values().stream().mapToLong(User::getPlayedTimeRaw).sum();
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

    @Override
    public String toString() {
        String name = getLastKnownName();
        return name == null ? "none" : name;
    }

    @Nullable
    public String getLastKnownName() {
        for (User user : new HashSet<>(this.userRecord.values())) {
            if (user.getLastKnownName() != null)
                return user.getLastKnownName();
        }
        return null;
    }
}