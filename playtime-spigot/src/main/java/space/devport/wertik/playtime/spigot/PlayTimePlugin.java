package space.devport.wertik.playtime.spigot;

import lombok.Getter;
import space.devport.utils.DevportPlugin;
import space.devport.wertik.playtime.storage.IUserStorage;
import space.devport.wertik.playtime.storage.json.JsonStorage;
import space.devport.wertik.playtime.storage.mysql.MySQLStorage;
import space.devport.wertik.playtime.storage.struct.StorageType;
import space.devport.wertik.playtime.system.GlobalUserManager;
import space.devport.wertik.playtime.system.LocalUserManager;

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

        new PlayTimeLanguage();

        registerPlaceholders();
    }

    @Override
    public void onPluginDisable() {

    }

    @Override
    public void onReload() {
        registerPlaceholders();
    }

    private void initiateStorage() {
        StorageType storageType = StorageType.fromString(configuration.getString("storage.type", "json"));

        IUserStorage userStorage;
        switch (storageType) {
            default:
            case JSON:
                userStorage = new JsonStorage();
                break;
            case MYSQL:
                userStorage = new MySQLStorage();
                break;
        }
        userStorage.initialize();
    }

    private void registerPlaceholders() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            //TODO unregister old
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
