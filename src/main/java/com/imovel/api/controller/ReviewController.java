package com.imovel.api.controller;

import com.imovel.api.exception.ResourceNotFoundException;
import com.imovel.api.response.StandardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Review management endpoints
 *
 * <p>Handles all operations related to property reviews including:
 * <ul>
 *   <li>Retrieving reviews for a property</li>
 *   <li>Adding new reviews</li>
 * </ul>
 *
 * <p>All endpoints return responses in the StandardResponse format
 * and any exceptions are automatically handled by the global exception handler.
 */
@RestController
@RequestMapping("/api/properties/{propertyId}/reviews")
public class ReviewController {

    /**
     * Retrieves all reviews for a specific property
     *
     * @param propertyId The ID of the property to get reviews for
     * @return ResponseEntity containing StandardResponse with list of reviews
     * @throws ResourceNotFoundException if the property doesn't exist
     */
    @GetMapping
    public ResponseEntity<StandardResponse<?>> getPropertyReviews(@PathVariable String propertyId) {
        // TODO: Implement get property reviews logic
        // Implementation should:
        // 1. Validate property exists (throw ResourceNotFoundException if not)
        // 2. Retrieve reviews from service layer
        // 3. Return success response with reviews data

        return ResponseEntity.ok(StandardResponse.success(null));
    }

    /**
     * Adds a new review for a specific property
     *
     * @param propertyId The ID of the property to add review to
     * @return ResponseEntity containing StandardResponse with created review
     * @throws ResourceNotFoundException if the property doesn't exist
     */
    @PostMapping
    public ResponseEntity<StandardResponse<?>> addReview(@PathVariable String propertyId) {
        // TODO: Implement add review logic
        // Implementation should:
        // 1. Validate property exists (throw ResourceNotFoundException if not)
        // 2. Validate review data (throw ValidationException if invalid)
        // 3. Check authorization (throw AuthorizationException if not authorized)
        // 4. Create review via service layer
        // 5. Return success response with created review

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(StandardResponse.success(null));
    }
}