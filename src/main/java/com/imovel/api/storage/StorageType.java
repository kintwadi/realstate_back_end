package com.imovel.api.storage;

import java.util.Arrays;

public enum StorageType {
    S3, DATABASE, LOCAL;

    public static StorageType getValue(String value) {
        try {
            return StorageType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unknown storage type: " + value + ". Supported values are: " +
                            Arrays.toString(values())
            );
        }
    }
}
