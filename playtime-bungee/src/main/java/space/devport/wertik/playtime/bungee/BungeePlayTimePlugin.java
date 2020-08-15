package space.devport.wertik.playtime.bungee;

import lombok.Getter;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import space.devport.wertik.playtime.TaskChainFactoryHolder;
import space.devport.wertik.playtime.bungee.console.BungeeConsoleOutput;
import space.devport.wertik.playtime.bungee.events.BungeePlayTimeDisableEvent;
import space.devport.wertik.playtime.bungee.listeners.BungeePlayerListener;
import space.devport.wertik.playtime.bungee.taskchain.BungeeTaskChainFactory;
import space.devport.wertik.playtime.system.LocalUserManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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

    @Getter
    private Configuration configuration;

    private ConfigurationProvider configurationProvider;

    @Override
    public void onEnable() {
        instance = this;

        this.consoleOutput = new BungeeConsoleOutput(this);

        TaskChainFactoryHolder.setTaskChainFactory(BungeeTaskChainFactory.create(this));

        configurationProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);
        loadConfig();

        getProxy().getPluginManager().registerListener(this, new BungeePlayerListener(this));
    }

    @Override
    public void onDisable() {
        getProxy().getPluginManager().callEvent(new BungeePlayTimeDisableEvent());
    }

    private void initiateStorage() {

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
            configurationProvider.load(file);
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
}