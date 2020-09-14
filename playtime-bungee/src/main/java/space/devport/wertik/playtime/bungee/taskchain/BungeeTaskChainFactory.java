package space.devport.wertik.playtime.bungee.taskchain;

import co.aikar.taskchain.*;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import space.devport.wertik.playtime.bungee.events.BungeePlayTimeDisableEvent;

import java.util.concurrent.TimeUnit;

/**
 * Attempt to mimic BukkitTaskChainFactory.
 */
public class BungeeTaskChainFactory extends TaskChainFactory {

    public static final TaskChainAbortAction<ProxiedPlayer, String, ?> MESSAGE = new TaskChainAbortAction<ProxiedPlayer, String, Object>() {
        public void onAbort(TaskChain<?> chain, ProxiedPlayer player, String message) {
            player.sendMessage(new TextComponent(message));
        }
    };

    public static final TaskChainAbortAction<ProxiedPlayer, String, ?> COLOR_MESSAGE = new TaskChainAbortAction<ProxiedPlayer, String, Object>() {
        public void onAbort(TaskChain<?> chain, ProxiedPlayer player, String message) {
            player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', message)));
        }
    };

    private BungeeTaskChainFactory(Plugin plugin, AsyncQueue asyncQueue) {
        super(new BungeeTaskChainFactory.BungeeGameInterface(plugin, asyncQueue));
    }

    public static TaskChainFactory create(Plugin plugin) {
        return new BungeeTaskChainFactory(plugin, new TaskChainAsyncQueue());
    }

    public static class BungeeGameInterface implements GameInterface {
        private final Plugin plugin;
        private final AsyncQueue asyncQueue;

        BungeeGameInterface(Plugin plugin, AsyncQueue asyncQueue) {
            this.plugin = plugin;
            this.asyncQueue = asyncQueue;
        }

        public AsyncQueue getAsyncQueue() {
            return this.asyncQueue;
        }

        /**
         * Let's just assume it's never the main thread.
         * Bungee shouldn't use the main thread anyway, no?
         */
        @Override
        public boolean isMainThread() {
            return false;
        }

        public void postToMain(Runnable run) {
            ProxyServer.getInstance().getScheduler().schedule(this.plugin, run, 2, TimeUnit.SECONDS);
        }

        public void scheduleTask(int ticks, Runnable run) {
            ProxyServer.getInstance().getScheduler().schedule(this.plugin, run, (long) ticks / 20L, TimeUnit.SECONDS);
        }

        public void registerShutdownHandler(TaskChainFactory factory) {

            ProxyServer.getInstance().getPluginManager().registerListener(this.plugin, new Listener() {
                @EventHandler
                public void onPluginDisable(BungeePlayTimeDisableEvent event) {
                    factory.shutdown(60, TimeUnit.SECONDS);
                }
            });
        }
    }
}
