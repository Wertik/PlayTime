package space.devport.wertik.playtime.bungee.console;

import space.devport.wertik.playtime.bungee.BungeePlayTimePlugin;
import space.devport.wertik.playtime.console.AbstractConsoleOutput;

public class BungeeConsoleOutput extends AbstractConsoleOutput {

    private final BungeePlayTimePlugin plugin;

    public BungeeConsoleOutput(BungeePlayTimePlugin plugin) {
        this.plugin = plugin;
        AbstractConsoleOutput.setImplementation(this);
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