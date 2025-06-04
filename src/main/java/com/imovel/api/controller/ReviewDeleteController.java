package com.imovel.api.controller;

import com.imovel.api.exception.ResourceNotFoundException;
import com.imovel.api.exception.AuthorizationException;
import com.imovel.api.response.StandardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling review deletion operations.
 * Uses StandardResponse for consistent API responses and leverages the enhanced exception handling system.
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewDeleteController {

    /**
     * Deletes a review by its ID.
     *
     * @param id The ID of the review to delete
     * @return ResponseEntity with StandardResponse indicating success or failure
     *
     * @throws ResourceNotFoundException if review with given ID doesn't exist
     * @throws AuthorizationException if user is not authorized to delete the review
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteReview(@PathVariable String id) {
        // TODO: Implement delete review logic
        // Implementation should:
        // 1. Verify review exists (throw ResourceNotFoundException if not)
        // 2. Check authorization (throw AuthorizationException if not authorized)
        // 3. Perform deletion
        // 4. Return appropriate response

        // Placeholder for actual implementation
        // This would be replaced with actual service call:
        // reviewService.deleteReview(id, currentUserId);

        return ResponseEntity.ok(StandardResponse.success(null));
    }
}