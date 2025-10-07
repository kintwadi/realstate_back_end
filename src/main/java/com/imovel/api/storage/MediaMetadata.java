package com.imovel.api.storage;

import java.time.Instant;

public interface MediaMetadata {
    String getName();
    String getType();
    long getSize();
    String getUrl();
    Instant getUploadDate();
    String getDescription();
    Long getPropertyId();
}
