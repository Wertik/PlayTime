package space.devport.wertik.playtime.system;

import lombok.Getter;
import lombok.Setter;

public class DataManager {

    private static DataManager instance;

    @Getter
    @Setter
    private GlobalUserManager globalUserManager;
    @Getter
    @Setter
    private LocalUserManager localUserManager;

    public static DataManager getInstance() {
        return instance == null ? new DataManager() : instance;
    }

    private DataManager() {
        instance = this;
    }
}