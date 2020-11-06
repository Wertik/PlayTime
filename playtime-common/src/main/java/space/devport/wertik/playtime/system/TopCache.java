package space.devport.wertik.playtime.system;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import space.devport.wertik.playtime.console.CommonLogger;
import space.devport.wertik.playtime.struct.User;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TopCache implements Runnable {

    @Getter
    private final String serverName;

    private final List<User> topUsers = new LinkedList<>();

    private final TopLoader topLoader;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    private int capacity;

    private ScheduledFuture<?> updateTask;

    public TopCache(String serverName, TopLoader topLoader, int capacity) {
        this.serverName = serverName;
        this.topLoader = topLoader;
        this.capacity = capacity;
    }

    public void resize(int max) {
        int old = this.capacity;
        this.capacity = max;
        CommonLogger.getImplementation().debug("Resized " + serverName + " top cache from " + old + " to " + capacity);
        load();
    }

    public void load() {
        topLoader.load(serverName, capacity).thenAccept(topUsers -> {
            this.topUsers.clear();
            this.topUsers.addAll(topUsers);
            CommonLogger.getImplementation().debug("Loaded " + serverName + " top cache to " + capacity);
        });
    }

    @Nullable
    public User getPosition(int position) {
        if (position <= 0)
            return null;

        if (position > topUsers.size()) {
            resize(position);
            return null;
        }
        return topUsers.get(position - 1);
    }

    public interface TopLoader {
        CompletableFuture<List<User>> load(String server, int position);
    }

    public void startUpdate(int interval) {
        if (updateTask != null)
            stop();

        this.updateTask = scheduledExecutorService.scheduleAtFixedRate(this, 0, interval, TimeUnit.SECONDS);
        CommonLogger.getImplementation().info("Started " + serverName + " top cache refresh at an interval of " + interval);
    }

    public void stop() {
        this.updateTask.cancel(false);
        this.updateTask = null;
        CommonLogger.getImplementation().info("Stopped " + serverName + " top cache refresh");
    }

    @Override
    public void run() {
        load();
    }
}
