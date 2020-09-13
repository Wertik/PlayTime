package space.devport.wertik.playtime.storage.struct;

import com.google.common.base.Strings;
import org.jetbrains.annotations.NotNull;

public enum StorageType {

    JSON, MYSQL;

    public static StorageType DEFAULT_STORAGE_TYPE = StorageType.JSON;

    @NotNull
    public static StorageType fromString(String str) {
        if (Strings.isNullOrEmpty(str)) return DEFAULT_STORAGE_TYPE;

        StorageType storageType;
        try {
            storageType = valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            return DEFAULT_STORAGE_TYPE;
        }
        return storageType;
    }
}