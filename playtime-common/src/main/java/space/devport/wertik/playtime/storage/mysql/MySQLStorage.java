package space.devport.wertik.playtime.storage.mysql;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import space.devport.wertik.playtime.MySQLConnection;
import space.devport.wertik.playtime.TaskChainFactoryHolder;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.struct.User;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class MySQLStorage implements IUserStorage {

    @Getter
    private MySQLConnection connection;

    @Getter
    @Setter
    private String tableName;

    public MySQLStorage(MySQLConnection connection, String tableName) {
        this.connection = connection;
        this.tableName = tableName;
    }

    @Override
    public void initialize() {
        TaskChainFactoryHolder.newChain().async(() -> connection.execute(Query.CREATE_TABLE.get(tableName))).execute((exception, task) -> {
            if (exception != null) exception.printStackTrace();
        });
    }

    @Override
    public User loadUser(UUID uniqueID) {
        return null;
    }

    @Override
    public Set<User> loadAll() {
        return null;
    }

    @Override
    public void saveUser(User user) {

    }

    @Override
    public void saveAll(Set<User> users) {

    }

    @Override
    public void deleteUser(User user) {

    }

    @Override
    public void purge() {

    }

    @Override
    public void purge(Function<User, Boolean> conditions) {
        throw new NotImplementedException();
    }

    public void insertUser(UUID uuid, String time) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        TaskChainFactoryHolder.newChain().async(() -> connection.execute(Query.INSERT_USER.get(tableName), uuid.toString(), offlinePlayer.getName(), time)).execute((exception, task) -> {
            if (exception != null) exception.printStackTrace();
        });
    }

    public boolean hasTime(UUID uuid) {
        try {
            ResultSet rs = connection.executeQuery(Query.EXIST_CHECK.get(tableName), uuid.toString());
            if (rs.next()) return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public Map<String, Integer> getTopTen() {
        Map<String, Integer> topTen = new LinkedHashMap<>();
        try {
            ResultSet rs = connection.executeQuery(Query.GET_TOP_TEN.get(tableName));
            if (rs == null) return topTen;
            topTen.put(rs.getString("uuid"), Integer.valueOf(rs.getString("time")));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return topTen;
    }

    public void setTime(UUID uuid, String time) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        TaskChainFactoryHolder.newChain().async(() -> connection.execute(Query.UPDATE_USER.get(tableName), uuid.toString(), offlinePlayer.getName(), time)).execute((exception, task) -> {
            if (exception != null) exception.printStackTrace();
        });
    }

    public String getTime(UUID uuid) {
        try {
            ResultSet rs = connection.executeQuery(Query.GET_TIME.get(tableName), uuid.toString());
            if (rs == null) return "";
            return rs.getString("time");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return "";
    }
}