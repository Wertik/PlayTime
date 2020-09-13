package space.devport.wertik.playtime.bungee.console;

import space.devport.wertik.playtime.bungee.BungeePlayTimePlugin;
import space.devport.wertik.playtime.console.CommonLogger;

public class BungeeLogger extends CommonLogger {

    private final BungeePlayTimePlugin plugin;

    public BungeeLogger(BungeePlayTimePlugin plugin) {
        this.plugin = plugin;
        CommonLogger.setImplementation(this);
    }

    @Override
    public void err(String msg) {
        plugin.getLogger().info("ERROR: " + msg);
    }

    @Override
    public void warn(String msg) {
        plugin.getLogger().info("WARN: " + msg);
    }

    @Override
    public void info(String msg) {
        plugin.getLogger().info("INFO: " + msg);
    }

    @Override
    public void debug(String msg) {
        if (isDebug())
            plugin.getLogger().info("DEBUG: " + msg);
    }
}