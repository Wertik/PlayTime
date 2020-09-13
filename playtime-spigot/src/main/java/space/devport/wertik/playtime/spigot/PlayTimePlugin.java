package space.devport.wertik.playtime.spigot;

import co.aikar.taskchain.BukkitTaskChainFactory;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import space.devport.utils.DevportPlugin;
import space.devport.utils.UsageFlag;
import space.devport.utils.utility.VersionUtil;
import space.devport.wertik.playtime.MySQLConnection;
import space.devport.wertik.playtime.TaskChainFactoryHolder;
import space.devport.wertik.playtime.console.AbstractConsoleOutput;
import space.devport.wertik.playtime.spigot.commands.PlayTimeCommand;
import space.devport.wertik.playtime.spigot.commands.subcommands.CheckGlobalSubCommand;
import space.devport.wertik.playtime.spigot.commands.subcommands.CheckSubCommand;
import space.devport.wertik.playtime.spigot.commands.subcommands.ReloadSubCommand;
import space.devport.wertik.playtime.spigot.commands.subcommands.ResetSubCommand;
import space.devport.wertik.playtime.spigot.console.SpigotConsoleOutput;
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
    private String durationFormat;

    @Getter
    private LocalUserManager localUserManager;

    @Getter
    private GlobalUserManager globalUserManager;

    @Override
    public void onPluginEnable() {
        instance = this;

        AbstractConsoleOutput.setImplementation(new SpigotConsoleOutput(consoleOutput));
        TaskChainFactoryHolder.setTaskChainFactory(BukkitTaskChainFactory.create(this));

        loadOptions();

        this.localUserManager = new SpigotLocalUserManager(this, initiateStorage());
        this.localUserManager.loadAll(Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toSet()));

        //TODO
        //this.globalUserManager = new GlobalUserManager();

        new PlayTimeLanguage();

        registerPlaceholders();

        registerListener(new PlayerListener(this));

        addMainCommand(new PlayTimeCommand())
                .addSubCommand(new ReloadSubCommand())
                .addSubCommand(new CheckGlobalSubCommand())
                .addSubCommand(new CheckSubCommand())
                .addSubCommand(new ResetSubCommand());
    }

    @Override
    public void onPluginDisable() {
        this.localUserManager.saveAll();
    }

    /**
     * Load additional config options.
     */
    public void loadOptions() {
        this.durationFormat = configuration.getString("formats.duration", "H'h' m'm' s's'");
    }

    @Override
    public void onReload() {
        registerPlaceholders();
        loadOptions();
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

                //TODO change table name to server names when appropriate
                // Add server names with mysql information into config.yml
                userStorage = new MySQLStorage(connection, this.configuration.getString("storage.mysql.table", "playtime"));
                break;
        }

        userStorage.initialize();
        return userStorage;
    }

    private void registerPlaceholders() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {

            // On version 2.10.9+ attempt to unregister expansion.
            if (PlaceholderAPI.isRegistered("playtime") &&
                    VersionUtil.compareVersions("2.10.9", PlaceholderAPIPlugin.getInstance().getDescription().getVersion()) < 1) {

                PlaceholderExpansion expansion = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().getExpansion("playtime");

                if (expansion != null) {
                    // Compare versions
                    if (compileVersionNumber(expansion.getVersion()) < compileVersionNumber(getDescription().getVersion())) {
                        expansion.unregister();
                        consoleOutput.info("Unregistered old playtime expansion (" + expansion.getVersion() + ")");
                    }
                }
            }

            new PlayTimeExpansion().register();
            consoleOutput.info("Found PlaceholderAPI! &aRegistered expansion.");
        }
    }

    private int compileVersionNumber(String versionString) {
        int versionNumber = 0;
        try {
            versionNumber = Integer.parseInt(versionString.replace("\\.", ""));
        } catch (NumberFormatException ignored) {
        }
        return versionNumber;
    }

    @Override
    public UsageFlag[] usageFlags() {
        return new UsageFlag[]{UsageFlag.LANGUAGE, UsageFlag.CONFIGURATION, UsageFlag.COMMANDS};
    }
}