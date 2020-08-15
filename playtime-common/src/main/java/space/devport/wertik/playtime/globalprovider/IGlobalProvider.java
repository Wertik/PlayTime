package space.devport.wertik.playtime.globalprovider;

import space.devport.wertik.playtime.struct.GlobalUser;

import java.util.UUID;

public interface IGlobalProvider {

    /**
     * Compose a GlobalUser object from table data.
     */
    GlobalUser loadUser(UUID uniqueID);
}
