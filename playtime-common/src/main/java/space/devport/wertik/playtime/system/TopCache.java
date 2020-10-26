package space.devport.wertik.playtime.system;

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

    private int capacity;

    private final List<User> topUsers = new LinkedList<>();

    private final TopLoader topLoader;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

    private ScheduledFuture<?> updateTask;

    public TopCache(TopLoader topLoader, int capacity) {
        this.topLoader = topLoader;
        this.capacity = capacity;
    }

    public void resize(String server, int max) {
        this.capacity = max;
        CommonLogger.getImplementation().debug("Resized top cache to " + capacity);
        load(server);
    }

    public void load(String server) {
        topLoader.load(server, capacity).thenAccept(topUsers -> {
            this.topUsers.clear();
            this.topUsers.addAll(topUsers);
            CommonLogger.getImplementation().debug("Loaded top cache to position " + capacity);
        });
    }

    @Nullable
    public User getPosition(String server, int position) {
        if (position <= 0)
            return null;

        if (position > topUsers.size()) {
            resize(server, position);
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
        CommonLogger.getImplementation().info("Started top cache update task at an interval of " + interval);
    }

    public void stop() {
        this.updateTask.cancel(false);
        this.updateTask = null;
        CommonLogger.getImplementation().info("Stopped top cache update task.");
    }

    @Override
    public void run() {

    }
}
