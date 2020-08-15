package space.devport.wertik.playtime.spigot;

import co.aikar.taskchain.BukkitTaskChainFactory;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import space.devport.utils.DevportPlugin;
import space.devport.wertik.playtime.MySQLConnection;
import space.devport.wertik.playtime.TaskChainFactoryHolder;
import space.devport.wertik.playtime.spigot.commands.PlayTimeCommand;
import space.devport.wertik.playtime.spigot.commands.subcommands.CheckGlobalSubCommand;
import space.devport.wertik.playtime.spigot.commands.subcommands.CheckSubCommand;
import space.devport.wertik.playtime.spigot.commands.subcommands.ReloadSubCommand;
import space.devport.wertik.playtime.spigot.listeners.PlayerListener;
import space.devport.wertik.playtime.spigot.system.SpigotLocalUserManager;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.storage.json.JsonStorage;
import space.devport.wertik.playtime.storage.mysql.MySQLStorage;
import space.devport.wertik.playtime.storage.struct.StorageType;
import space.devport.wertik.playtime.system.GlobalUserManager;
import space.devport.wertik.playtime.system.LocalUserManager;

import java.util.stream.Collectors;

public class PlayTimePlugin extends DevportPlugin {

    @Getter
    private static PlayTimePlugin instance;

    @Getter
    private LocalUserManager localUserManager;

    @Getter
    private GlobalUserManager globalUserManager;

    @Override
    public void onPluginEnable() {
        instance = this;

        TaskChainFactoryHolder.setTaskChainFactory(BukkitTaskChainFactory.create(this));

        this.localUserManager = new SpigotLocalUserManager(this, initiateStorage());
        //TODO maybe replace with a for loop, it's faster.
        this.localUserManager.loadAll(Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toSet()));

        //TODO
        //this.globalUserManager = new GlobalUserManager();

        new PlayTimeLanguage();

        registerPlaceholders();

        registerListener(new PlayerListener(this));

        addMainCommand(new PlayTimeCommand())
                .addSubCommand(new ReloadSubCommand())
                .addSubCommand(new CheckGlobalSubCommand())
                .addSubCommand(new CheckSubCommand());
    }

    @Override
    public void onPluginDisable() {
    }

    @Override
    public void onReload() {
        registerPlaceholders();
    }

    private IUserStorage initiateStorage() {
        StorageType storageType = StorageType.fromString(configuration.getString("storage.type", "json"));

        IUserStorage userStorage;
        switch (storageType) {
            default:
            case JSON:
                userStorage = new JsonStorage();
                break;
            case MYSQL:
                MySQLConnection connection = new MySQLConnection(configuration.getString("storage.mysql.host"),
                        getConfig().getInt("storage.mysql.port"),
                        getConfig().getString("storage.mysql.username"),
                        getConfig().getString("storage.mysql.password"),
                        getConfig().getString("storage.mysql.database"),
                        getConfig().getInt("storage.mysql.pool-size", 10));
                connection.connect();

                //TODO change table name
                userStorage = new MySQLStorage(connection, "play-time");
                break;
        }

        userStorage.initialize();
        return userStorage;
    }

    private void registerPlaceholders() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            //TODO unregister old expansion
            new PlayTimeExpansion().register();
            consoleOutput.info("Found PlaceholderAPI! &aRegistered expansion.");
        }
    }

    @Override
    public boolean useLanguage() {
        return true;
    }

    @Override
    public boolean useHolograms() {
        return false;
    }

    @Override
    public boolean useMenus() {
        return false;
    }
}
