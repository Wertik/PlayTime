package space.devport.wertik.playtime.struct;

import lombok.Getter;

import java.util.Objects;

public class ServerInfo {

    @Getter
    private final String name;
    @Getter
    private boolean networkWide = false;

    public ServerInfo(String name) {
        this.name = name;
    }

    public ServerInfo(String name, boolean networkWide) {
        this.name = name;
        this.networkWide = networkWide;
    }

    @Override
    public String toString() {
        return name + ";" + networkWide;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerInfo that = (ServerInfo) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}