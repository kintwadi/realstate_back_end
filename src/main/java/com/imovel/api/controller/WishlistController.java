package com.imovel.api.controller;

import com.imovel.api.response.StandardResponse;
import org.springframework.web.bind.annotation.*;

/**
 * Wishlist management endpoints
 */
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @GetMapping
    public StandardResponse<Void> getWishlist() {
        // TODO: Implement get wishlist logic
        return new StandardResponse<>();
    }

    @PostMapping("/{propertyId}")
    public StandardResponse<Void> addToWishlist(@PathVariable String propertyId) {
        // TODO: Implement add to wishlist logic
        return new StandardResponse<>();
    }

    @DeleteMapping("/{propertyId}")
    public StandardResponse<Void> removeFromWishlist(@PathVariable String propertyId) {
        // TODO: Implement remove from wishlist logic
        return new StandardResponse<>();
    }

    @DeleteMapping
    public StandardResponse<Void> clearWishlist() {
        // TODO: Implement clear wishlist logic
        return new StandardResponse<>();
    }
}