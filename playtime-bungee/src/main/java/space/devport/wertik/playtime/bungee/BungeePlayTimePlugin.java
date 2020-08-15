package space.devport.wertik.playtime.bungee;

import lombok.Getter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import space.devport.wertik.playtime.MySQLConnection;
import space.devport.wertik.playtime.TaskChainFactoryHolder;
import space.devport.wertik.playtime.bungee.commands.BungeePlayTimeCommand;
import space.devport.wertik.playtime.bungee.console.BungeeConsoleOutput;
import space.devport.wertik.playtime.bungee.events.BungeePlayTimeDisableEvent;
import space.devport.wertik.playtime.bungee.listeners.BungeePlayerListener;
import space.devport.wertik.playtime.bungee.taskchain.BungeeTaskChainFactory;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.storage.json.JsonStorage;
import space.devport.wertik.playtime.storage.mysql.MySQLStorage;
import space.devport.wertik.playtime.storage.struct.StorageType;
import space.devport.wertik.playtime.system.LocalUserManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

public class BungeePlayTimePlugin extends Plugin {

    @Getter
    private static BungeePlayTimePlugin instance;

    /**
     * Bungee only caches local user data as there's no reason to keep them here.
     */
    @Getter
    private LocalUserManager localUserManager;

    @Getter
    private BungeeConsoleOutput consoleOutput;

    private ConfigurationProvider configurationProvider;

    @Getter
    private Configuration configuration;

    @Getter
    private String durationFormat;

    @Override
    public void onEnable() {
        instance = this;

        this.consoleOutput = new BungeeConsoleOutput(this);

        TaskChainFactoryHolder.setTaskChainFactory(BungeeTaskChainFactory.create(this));

        configurationProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);

        loadConfig();
        loadOptions();

        this.localUserManager = new LocalUserManager(initiateStorage());
        this.localUserManager.loadAll(ProxyServer.getInstance().getPlayers().stream().map(ProxiedPlayer::getUniqueId).collect(Collectors.toSet()));

        getProxy().getPluginManager().registerListener(this, new BungeePlayerListener(this));

        getProxy().getPluginManager().registerCommand(this, new BungeePlayTimeCommand());
    }

    @Override
    public void onDisable() {
        this.localUserManager.saveAll();

        getProxy().getPluginManager().callEvent(new BungeePlayTimeDisableEvent());
    }

    public void reload(CommandSender sender) {
        long start = System.currentTimeMillis();

        loadConfig();
        loadOptions();

        sender.sendMessage(new TextComponent("&7Done... reload took &f" + (System.currentTimeMillis() - start) + "&7ms."));
    }

    public void loadOptions() {
        this.consoleOutput.setDebug(configuration.getBoolean("debug-enabled", false));
        this.durationFormat = configuration.getString("formats.duration", "H'h' m'm' s's'");
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

    public void loadConfig() {

        File file = new File(getDataFolder(), "config.yml");

        if (!file.exists()) {
            if (!copy(file, "config.yml"))
                consoleOutput.err("Could not create config.yml");
            else
                consoleOutput.debug("Created new config.yml");
        }

        try {
            this.configuration = configurationProvider.load(file);
        } catch (IOException e) {
            if (consoleOutput.isDebug())
                e.printStackTrace();
            consoleOutput.err("Could not load config.yml");
        }
    }

    public void saveConfig() {

        File file = new File(getDataFolder(), "config.yml");

        try {
            configurationProvider.save(configuration, file);
        } catch (IOException e) {
            consoleOutput.err("Could not save config.yml");
        }
    }

    private boolean copy(File file, String resource) {
        try {
            Files.copy(getResourceAsStream(resource), file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().warning("Could not copy " + resource);
            return false;
        }
        return true;
    }

    public Configuration getConfig() {
        return configuration;
    }
}