package space.devport.wertik.playtime.system;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LoadCache<K, V> {

    private final Map<K, CompletableFuture<V>> loading = new HashMap<>();

    public void setLoading(K key, CompletableFuture<V> future) {
        loading.put(key, future);
    }

    public void setLoaded(K key) {
        loading.remove(key);
    }

    public CompletableFuture<V> getLoading(K key) {
        return isLoading(key) ? loading.get(key) : CompletableFuture.supplyAsync(() -> null);
    }

    public boolean isLoading(K key) {
        return loading.containsKey(key);
    }
}
