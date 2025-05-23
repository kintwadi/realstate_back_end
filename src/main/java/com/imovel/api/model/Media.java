package com.imovel.api.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Represents media files associated with properties
 */
@Data
public class Media {
    private String id;
    private String url;
    private MediaType type;
    private String format;
    private Integer width;
    private Integer height;
    private Long size;
    private Integer duration;
    private Boolean isPrimary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}