package space.devport.wertik.playtime;

import lombok.Getter;

public class ServerConnection {

    // Server name
    @Getter
    private final String name;

    // Table name
    @Getter
    private final String table;

    public ServerConnection(String host, int port, String user, String password, String databaseName, int poolSize) {
        super(host, port, user, password, databaseName, poolSize);
    }
}