package space.devport.wertik.playtime;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConnectionManager {

    private static ConnectionManager instance;

    private final Map<String, ServerConnection> connections = new HashMap<>();

    public static ConnectionManager getInstance() {
        return instance == null ? new ConnectionManager() : instance;
    }

    private ConnectionManager() {
        instance = this;
    }

    public ServerConnection initializeConnection(String name, ConnectionInfo info) {
        ServerConnection serverConnection = attemptReroute(info);
        this.connections.put(name, serverConnection);
        return serverConnection;
    }

    /**
     * If a request for connection is similar to one already connected, reroute there.
     */
    @NotNull
    public ServerConnection attemptReroute(ConnectionInfo connectionInfo) {
        for (Map.Entry<String, ServerConnection> entry : this.connections.entrySet()) {
            if (entry.getValue().getConnectionInfo().compare(connectionInfo)) {
                return entry.getValue();
            }
        }
        return new ServerConnection(connectionInfo);
    }

    public void closeConnection(String name) {
        if (!this.connections.containsKey(name)) return;

        this.connections.get(name).close();
        this.connections.remove(name);
        //TODO Find all reroutes and migrate if possible.
    }

    public Map<String, ServerConnection> getConnections() {
        return Collections.unmodifiableMap(this.connections);
    }

    public ServerConnection getConnection(String name) {
        return this.connections.get(name);
    }

    public void addConnection(String name, ServerConnection serverConnection) {
        this.connections.put(name, serverConnection);
    }
}