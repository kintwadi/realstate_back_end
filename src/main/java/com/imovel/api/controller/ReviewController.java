package com.imovel.api.controller;

import com.imovel.api.response.StandardResponse;
import org.springframework.web.bind.annotation.*;

/**
 * Review management endpoints
 */
@RestController
@RequestMapping("/api/properties/{propertyId}/reviews")
public class ReviewController {

    @GetMapping
    public StandardResponse<Void> getPropertyReviews(@PathVariable String propertyId) {
        // TODO: Implement get property reviews logic
        return new StandardResponse<>();
    }

    @PostMapping
    public StandardResponse<Void> addReview(@PathVariable String propertyId) {
        // TODO: Implement add review logic
        return new StandardResponse<>();
    }
}