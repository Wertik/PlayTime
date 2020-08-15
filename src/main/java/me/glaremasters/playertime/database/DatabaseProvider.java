package me.glaremasters.playertime.database;

import java.util.Map;
import java.util.UUID;

public interface DatabaseProvider {

    void initialize();

    void insertUser(UUID uuid, String time);

    boolean hasTime(UUID uuid);

    void setTime(UUID uuid, String time);

    String getTime(UUID uuid);

    Map<String, Integer> getTopTen();
}