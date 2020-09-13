package space.devport.wertik.playtime;

import org.jetbrains.annotations.NotNull;
import space.devport.wertik.playtime.console.AbstractConsoleOutput;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
        AbstractConsoleOutput.getImplementation().debug("Attempting to connect to " + name + ", info: " + info.toString());
        ServerConnection serverConnection = attemptReroute(info);

        if (!serverConnection.isConnected()) {
            try {
                serverConnection.connect();
            } catch (IllegalStateException e) {
                if (AbstractConsoleOutput.getImplementation().isDebug())
                    e.printStackTrace();
                return null;
            }
        }

        this.connections.put(name, serverConnection);
        AbstractConsoleOutput.getImplementation().debug("Initialized and registered connection to " + name + ", info: " + info);
        return serverConnection;
    }

    /**
     * If a request for connection is similar to one already connected, reroute there.
     */
    @NotNull
    public ServerConnection attemptReroute(ConnectionInfo connectionInfo) {
        for (Map.Entry<String, ServerConnection> entry : this.connections.entrySet()) {
            if (entry.getValue().getConnectionInfo().compare(connectionInfo)) {
                AbstractConsoleOutput.getImplementation().debug("Rerouting to " + entry.getKey());
                return entry.getValue();
            }
        }
        return new ServerConnection(connectionInfo);
    }

    public void closeConnections() {
        new HashSet<>(this.connections.keySet()).forEach(this::closeConnection);
    }

    public void closeConnection(String name) {
        if (!this.connections.containsKey(name) || !this.connections.get(name).isConnected()) return;

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