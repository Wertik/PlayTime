package space.devport.wertik.playtime.storage.struct;

import com.google.common.base.Strings;

public enum StorageType {
    JSON, MYSQL;

    public static StorageType fromString(String str) {
        if (Strings.isNullOrEmpty(str)) return null;

        StorageType storageType;
        try {
            storageType = valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
        return storageType;
    }
}