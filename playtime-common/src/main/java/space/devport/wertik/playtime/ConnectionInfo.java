package space.devport.wertik.playtime;

import lombok.Getter;

public class ConnectionInfo {

    @Getter
    private final String host;
    @Getter
    private final int port;
    @Getter
    private final String username;
    @Getter
    private final String password;
    @Getter
    private final String database;

    public ConnectionInfo(String host, int port, String username, String password, String database) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    public ConnectionInfo(ConnectionInfo info) {
        this.host = info.getHost();
        this.port = info.getPort();
        this.username = info.getUsername();
        this.password = info.getPassword();
        this.database = info.getDatabase();
    }

    @Override
    public String toString() {
        return username + "@" + host + ":" + port + "/" + database + " -p " + password;
    }

    /**
     * Don't override #equals().
     */
    public boolean compare(ConnectionInfo info) {
        return this.host.equals(info.getHost()) &&
                this.port == info.getPort() &&
                this.username.equals(info.getUsername()) &&
                this.database.equals(info.getDatabase());
    }
}