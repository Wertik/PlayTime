package me.glaremasters.playertime.database.yml;

import me.glaremasters.playertime.PlayerTime;
import me.glaremasters.playertime.database.DatabaseProvider;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.*;

/**
 * Created by GlareMasters
 * Date: 7/19/2018
 * Time: 10:18 PM
 */
public class YamlDatabaseProvider implements DatabaseProvider {

    private final PlayerTime playerTime = PlayerTime.getInstance();

    @Override
    public void initialize() {
        System.out.println("Initializing PlayerTime storage file...");
    }

    @Override
    public void insertUser(UUID uuid, String time) {
        playerTime.config.set(uuid.toString(), time);
        playerTime.saveTime();
    }

    @Override
    public boolean hasTime(UUID uuid) {
        return PlayerTime.getInstance().config.getString(uuid.toString()) != null;
    }

    @Override
    public void setTime(UUID uuid, String time) {
        playerTime.config.set(time, uuid.toString());
        playerTime.saveTime();
    }

    @Override
    public String getTime(UUID uuid) {
        return PlayerTime.getInstance().config.getString(uuid.toString());
    }

    @Override
    public Map<String, Integer> getTopTen() {
        Map<String, Integer> topTen = new LinkedHashMap<>();

        for (String key : playerTime.config.getKeys(false)) {
            topTen.put(key, Integer.valueOf(playerTime.config.getString(key)));
        }

        List<Map.Entry<String, Integer>> list = new LinkedList<>(topTen.entrySet());
        list.sort((o1, o2) -> o2.getValue() - o1.getValue());

        topTen.clear();

        for (Map.Entry<String, Integer> aList : list) {
            topTen.put(aList.getKey(), aList.getValue());
        }

        return topTen;
    }

}
