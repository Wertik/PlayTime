package space.devport.wertik.playtime.spigot;

import com.google.common.base.Strings;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import space.devport.utils.ConsoleOutput;
import space.devport.utils.DevportPlugin;
import space.devport.utils.UsageFlag;
import space.devport.utils.text.language.LanguageManager;
import space.devport.utils.utility.VersionUtil;
import space.devport.wertik.playtime.console.CommonLogger;
import space.devport.wertik.playtime.mysql.ConnectionManager;
import space.devport.wertik.playtime.mysql.struct.ConnectionInfo;
import space.devport.wertik.playtime.mysql.struct.ServerConnection;
import space.devport.wertik.playtime.spigot.commands.PlayTimeCommand;
import space.devport.wertik.playtime.spigot.commands.subcommands.CheckGlobalSubCommand;
import space.devport.wertik.playtime.spigot.commands.subcommands.CheckSubCommand;
import space.devport.wertik.playtime.spigot.commands.subcommands.ReloadSubCommand;
import space.devport.wertik.playtime.spigot.commands.subcommands.ResetSubCommand;
import space.devport.wertik.playtime.spigot.commands.subcommands.TopSubCommand;
import space.devport.wertik.playtime.spigot.console.SpigotLogger;
import space.devport.wertik.playtime.spigot.listeners.PlayerListener;
import space.devport.wertik.playtime.spigot.system.SpigotLocalUserManager;
import space.devport.wertik.playtime.spigot.utils.SpigotCommonUtility;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.storage.json.JsonStorage;
import space.devport.wertik.playtime.storage.mysql.MySQLStorage;
import space.devport.wertik.playtime.storage.struct.StorageType;
import space.devport.wertik.playtime.struct.ServerInfo;
import space.devport.wertik.playtime.system.GlobalUserManager;
import space.devport.wertik.playtime.system.LocalUserManager;
import space.devport.wertik.playtime.utils.CommonUtility;

public class PlayTimePlugin extends DevportPlugin {

    @Getter
    private String durationFormat;

    @Getter
    private LocalUserManager localUserManager;

    @Getter
    private GlobalUserManager globalUserManager;

    @Override
    public void onPluginEnable() {
        CommonLogger.setImplementation(new SpigotLogger(consoleOutput));
        CommonUtility.setImplementation(new SpigotCommonUtility());

        loadOptions();

        this.localUserManager = new SpigotLocalUserManager(this, initiateStorage());
        this.localUserManager.loadOnline();

        this.globalUserManager = new GlobalUserManager();
        initializeRemotes();

        new PlayTimeLanguage(this);

        this.localUserManager.loadTop();
        this.globalUserManager.loadTop();

        this.globalUserManager.startTopUpdate(configuration.getFileConfiguration().getInt("top-cache-update-interval", 300));
        this.localUserManager.getTopCache().startUpdate(configuration.getFileConfiguration().getInt("top-cache-update-interval", 300));

        registerPlaceholders();

        registerListener(new PlayerListener(this));

        addMainCommand(new PlayTimeCommand())
                .addSubCommand(new ReloadSubCommand(this))
                .addSubCommand(new CheckGlobalSubCommand(this))
                .addSubCommand(new CheckSubCommand(this))
                .addSubCommand(new ResetSubCommand(this))
                .addSubCommand(new TopSubCommand(this));
    }

    @Override
    public void onPluginDisable() {
        HandlerList.unregisterAll(this);
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

    /**
     * Save data, reload connections, load data.
     */
    public void hardReload(CommandSender sender) {
        consoleOutput.addListener(sender);
        consoleOutput.info("Saving all data...");

        this.localUserManager.saveAll().thenRunAsync(() -> {
            consoleOutput.info("Closing connections...");
            ConnectionManager.getInstance().closeConnections();
        }).thenRunAsync(() -> {
            this.globalUserManager.dumpAll();

            reload(sender);

            consoleOutput.info("Initializing storages...");

            this.localUserManager = new SpigotLocalUserManager(this, initiateStorage());

            this.globalUserManager = new GlobalUserManager();
            initializeRemotes();

            this.localUserManager.loadOnline();
            Bukkit.getOnlinePlayers().forEach(p -> this.globalUserManager.loadGlobalUser(p.getUniqueId()));

            this.localUserManager.loadTop();
            this.globalUserManager.loadTop();

            this.globalUserManager.startTopUpdate(configuration.getFileConfiguration().getInt("top-cache-update-interval", 300));
            this.localUserManager.getTopCache().startUpdate(configuration.getFileConfiguration().getInt("top-cache-update-interval", 300));

            consoleOutput.removeListener(sender);
        }).exceptionally(e -> {
            ConsoleOutput.getInstance().err("An error occurred on hard reload: " + e.getMessage());
            e.printStackTrace();
            return null;
        });
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

    private void initializeRemotes() {
        if (!configuration.getFileConfiguration().getBoolean("use-remotes", false)) return;

        consoleOutput.info("Starting remote connections and cache...");

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

        fillServerNames();
    }

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
            CommonLogger.getImplementation().err("Could not create a local storage. Cannot function properly.");
            return null;
        }

        userStorage.initialize();
        return userStorage;
    }

    private void registerPlaceholders() {

        if (getPluginManager().getPlugin("PlaceholderAPI") == null)
            return;

        // On version 2.10.9+ unregister expansion.
        if (VersionUtil.compareVersions("2.10.9", getPluginManager().getPlugin("PlaceholderAPI").getDescription().getVersion()) < 1) {
            PlaceholderExpansion expansion = PlaceholderAPIPlugin.getInstance().getLocalExpansionManager().getExpansion("playtime");

            if (expansion != null) {
                Bukkit.getScheduler().runTask(this, () -> {
                    expansion.unregister();
                    new PlayTimeExpansion(this).register();
                });
                consoleOutput.info("Unregistered old playtime expansion (" + expansion.getVersion() + ")");
                return;
            }
        }

        Bukkit.getScheduler().runTask(this, () -> new PlayTimeExpansion(this).register());
        consoleOutput.info("Found PlaceholderAPI! &aRegistered expansion.");
    }

    public void fillServerNames() {
        for (ServerInfo info : globalUserManager.getRemoteStorages().keySet()) {
            getManager(LanguageManager.class).addDefault("Servers." + info.getName(), info.getName());
        }
    }

    public String translateServerName(String server) {
        String name = getManager(LanguageManager.class).getLanguage().getString("Servers." + server);
        return Strings.isNullOrEmpty(name) ? server : name;
    }

    @Override
    public UsageFlag[] usageFlags() {
        return new UsageFlag[]{UsageFlag.CONFIGURATION, UsageFlag.COMMANDS, UsageFlag.LANGUAGE};
    }

    public static PlayTimePlugin getInstance() {
        return getPlugin(PlayTimePlugin.class);
    }
}
