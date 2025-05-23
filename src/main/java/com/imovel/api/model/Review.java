package com.imovel.api.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Represents a review/rating for a property
 */
@Data
public class Review {
    private String id;
    private String propertyId;
    private String userId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}