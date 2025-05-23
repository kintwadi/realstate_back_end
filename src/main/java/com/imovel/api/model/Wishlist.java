package com.imovel.api.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a user's wishlist of properties
 */
@Data
public class Wishlist {
    private String id;
    private String userId;
    private List<String> propertyIds;
    private LocalDateTime createdAt;
}