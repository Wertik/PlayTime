package space.devport.wertik.playtime.spigot;

import co.aikar.taskchain.BukkitTaskChainFactory;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import space.devport.utils.DevportPlugin;
import space.devport.utils.UsageFlag;
import space.devport.utils.utility.VersionUtil;
import space.devport.wertik.playtime.*;
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
import space.devport.wertik.playtime.system.DataManager;
import space.devport.wertik.playtime.system.GlobalUserManager;
import space.devport.wertik.playtime.system.LocalUserManager;

import java.util.stream.Collectors;

public class PlayTimePlugin extends DevportPlugin {

    @Getter
    private String durationFormat;

    @Getter
    private LocalUserManager localUserManager;

    @Getter
    private GlobalUserManager globalUserManager;

    @Override
    public void onPluginEnable() {
        AbstractConsoleOutput.setImplementation(new SpigotConsoleOutput(consoleOutput));
        TaskChainFactoryHolder.setTaskChainFactory(BukkitTaskChainFactory.create(this));
        CommonUtility.setImplementation(new SpigotCommonUtility());

        loadOptions();

        this.localUserManager = new SpigotLocalUserManager(this, initiateStorage());
        DataManager.getInstance().setLocalUserManager(this.localUserManager);
        this.localUserManager.loadAll(Bukkit.getOnlinePlayers().stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toSet()));

        initializeRemote();

        new PlayTimeLanguage(this);

        registerPlaceholders();

        registerListener(new PlayerListener(this));

        addMainCommand(new PlayTimeCommand())
                .addSubCommand(new ReloadSubCommand(this))
                .addSubCommand(new CheckGlobalSubCommand(this))
                .addSubCommand(new CheckSubCommand(this))
                .addSubCommand(new ResetSubCommand(this));
    }

    @Override
    public void onPluginDisable() {
        HandlerList.unregisterAll(this);
        this.localUserManager.saveAll();
        ConnectionManager.getInstance().closeConnections();
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

    public ConnectionInfo loadInfo(String path) {
        ConfigurationSection section = configuration.getFileConfiguration().getConfigurationSection(path);
        if (section == null) return null;

        return new ConnectionInfo(section.getString("host", "localhost"),
                section.getInt("port", 3306),
                section.getString("username", "root"),
                section.getString("password"),
                section.getString("database"));
    }

    private void initializeRemote() {
        if (!configuration.getFileConfiguration().getBoolean("use-remotes", false)) return;

        consoleOutput.info("Starting remote connections and cache...");
        this.globalUserManager = new GlobalUserManager();
        DataManager.getInstance().setGlobalUserManager(this.globalUserManager);

        ConfigurationSection section = configuration.getFileConfiguration().getConfigurationSection("servers");
        if (section == null) return;

        for (String serverName : section.getKeys(false)) {
            ConnectionInfo connectionInfo = loadInfo("servers." + serverName);

            if (connectionInfo == null) return;

            connectionInfo.setReadOnly(true);

            globalUserManager.initializeStorage(serverName,
                    connectionInfo,
                    section.getString(serverName + ".table", serverName),
                    section.getBoolean(serverName + ".network-server", false));
        }
    }

    //TODO Merge with Bungee, same.
    private IUserStorage initiateStorage() {
        StorageType storageType = StorageType.fromString(configuration.getString("storage.type", "json"));

        IUserStorage userStorage = null;
        switch (storageType) {
            default:
            case JSON:
                userStorage = new JsonStorage();
                break;
            case MYSQL:
                ConnectionInfo connectionInfo = loadInfo("storage.mysql");
                ServerConnection serverConnection = ConnectionManager.getInstance().initializeConnection("local", connectionInfo);

                if (serverConnection != null)
                    userStorage = new MySQLStorage(serverConnection, configuration.getString("storage.mysql.table", "play-time"));
                break;
        }

        if (userStorage == null) {
            AbstractConsoleOutput.getImplementation().err("Could not create a local storage. Cannot function properly.");
            return null;
        }

        userStorage.initialize();
        return userStorage;
    }

    private void registerPlaceholders() {
        if (getPluginManager().getPlugin("PlaceholderAPI") != null) {

            // On version 2.10.9+ unregister expansion.
            if (VersionUtil.compareVersions("2.10.9", getPluginManager().getPlugin("PlaceholderAPI").getDescription().getVersion()) < 1) {
                PlaceholderExpansion expansion = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().getExpansion("playtime");

                if (expansion != null) {
                    expansion.unregister();
                    consoleOutput.info("Unregistered old playtime expansion (" + expansion.getVersion() + ")");
                }
            }

            new PlayTimeExpansion(this).register();
            consoleOutput.info("Found PlaceholderAPI! &aRegistered expansion.");
        }
    }

    @Override
    public UsageFlag[] usageFlags() {
        return new UsageFlag[]{UsageFlag.CONFIGURATION, UsageFlag.COMMANDS, UsageFlag.LANGUAGE};
    }

    public static PlayTimePlugin getInstance() {
        return getPlugin(PlayTimePlugin.class);
    }
}
