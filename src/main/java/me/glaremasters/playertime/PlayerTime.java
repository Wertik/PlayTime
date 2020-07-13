package me.glaremasters.playertime;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import me.glaremasters.playertime.commands.CMDCheck;
import me.glaremasters.playertime.commands.CMDReload;
import me.glaremasters.playertime.commands.CMDTop;
import me.glaremasters.playertime.database.DatabaseProvider;
import me.glaremasters.playertime.database.mysql.MySQLDatabaseProvider;
import me.glaremasters.playertime.database.yml.YamlDatabaseProvider;
import me.glaremasters.playertime.events.GUI;
import me.glaremasters.playertime.events.Leave;
import me.glaremasters.playertime.utils.SaveTask;
import me.glaremasters.playertime.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public final class PlayerTime extends JavaPlugin {

    private static PlayerTime instance;

    public static PlayerTime getInstance() {
        return instance;
    }

    private static TaskChainFactory taskChainFactory;

    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }

    public File playTime = new File(this.getDataFolder(), "playtime.yml");
    public YamlConfiguration config = YamlConfiguration.loadConfiguration(this.playTime);

    public DatabaseProvider getDatabase() {
        return database;
    }

    private DatabaseProvider database;

    @Override
    public void onEnable() {
        instance = this;

        checkConfig();
        saveDefaultConfig();
        saveTime();

        taskChainFactory = BukkitTaskChainFactory.create(this);

        setDatabaseType();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getLogger().info("Found PlaceholderAPI. Registering placeholders.");
            new PlayerTimeExpansion().register();
        }

        getServer().getPluginManager().registerEvents(new Leave(), this);
        getServer().getPluginManager().registerEvents(new GUI(), this);

        getCommand("ptcheck").setExecutor(new CMDCheck());
        getCommand("pttop").setExecutor(new CMDTop(this));
        getCommand("ptreload").setExecutor(new CMDReload(this));

        SaveTask.startTask();
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (instance.getDatabase().hasTime(player.getUniqueId().toString())) {
                instance.getDatabase().setTime(player.getUniqueId().toString(), String.valueOf(TimeUtil.getTimeFromStatistics(player)));
            } else {
                instance.getDatabase().insertUser(player.getUniqueId().toString(), String.valueOf(TimeUtil.getTimeFromStatistics(player)));
            }
        }
    }

    public void setDatabaseType() {
        switch (getConfig().getString("database.type", "yml").toLowerCase()) {
            case "mysql":
                database = new MySQLDatabaseProvider();
                break;
            case "yml":
                database = new YamlDatabaseProvider();
                break;
            default:
                database = new YamlDatabaseProvider();
                break;
        }
        database.initialize();
    }

    public void saveTime() {
        try {
            config.save(playTime);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Could not save PlayTime Data!");
            e.printStackTrace();
        }
    }

    private void checkConfig() {
        if (!getConfig().isSet("config-version") || getConfig().getInt("config-version") < 3) {
            File oldConfig = new File(getDataFolder(), "config.yml");
            File newConfig = new File(getDataFolder(), "config-old.yml");
            oldConfig.renameTo(newConfig);
        }
    }
}