package space.devport.wertik.playtime;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

public class ConnectionInfo {

    @Getter
    private String host;
    @Getter
    private int port;
    @Getter
    private String username;
    @Getter
    private String password;
    @Getter
    private String database;

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

    public HikariDataSource connect() {

        if (host == null) {
            throw new IllegalStateException("MySQL Connection not configured. Cannot continue.");
        }

        HikariDataSource hikari = new HikariDataSource();
        hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");

        hikari.addDataSourceProperty("serverName", host);
        hikari.addDataSourceProperty("port", port);
        hikari.addDataSourceProperty("databaseName", database);

        hikari.addDataSourceProperty("user", username);
        hikari.addDataSourceProperty("password", password);

        hikari.addDataSourceProperty("characterEncoding", "utf8");
        hikari.addDataSourceProperty("useUnicode", "true");

        hikari.validate();
        return hikari;
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