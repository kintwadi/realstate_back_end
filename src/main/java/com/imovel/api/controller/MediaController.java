package com.imovel.api.controller;

import com.imovel.api.exception.ResourceNotFoundException;
import com.imovel.api.response.StandardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Media management endpoints
 *
 * All endpoints return StandardResponse wrapped in ResponseEntity for consistent response structure
 * and proper HTTP status codes. Exceptions are automatically handled by the GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/media")
public class MediaController {

    /**
     * Uploads media to the system
     *
     * @return ResponseEntity containing StandardResponse with success status
     * @throws com.imovel.api.exception.ValidationException if upload request is invalid
     * @throws com.imovel.api.exception.ConflictException if media already exists
     */
    @PostMapping("/upload")
    public ResponseEntity<StandardResponse<Void>> uploadMedia() {
        // TODO: Implement upload media logic
        // Service layer would throw appropriate exceptions like:
        // - ValidationException for invalid requests
        // - ConflictException if media already exists
        // - ResourceNotFoundException if referenced resources don't exist

        return ResponseEntity.ok(StandardResponse.success(null));
    }

    /**
     * Retrieves media by its unique identifier
     *
     * @param mediaId The ID of the media to retrieve
     * @return ResponseEntity containing StandardResponse with media data
     * @throws ResourceNotFoundException if media with given ID doesn't exist
     */
    @GetMapping("/{mediaId}")
    public ResponseEntity<StandardResponse<String>> getMedia(@PathVariable String mediaId) {
        // TODO: Implement get media logic
        // Example service call that might throw:
        // Media media = mediaService.getMediaById(mediaId);
        // return ResponseEntity.ok(StandardResponse.success(media.getUrl()));
        return ResponseEntity.ok(StandardResponse.success(null));
    }

    /**
     * Deletes media by its unique identifier
     *
     * @param mediaId The ID of the media to delete
     * @return ResponseEntity with no content (204) on successful deletion
     * @throws ResourceNotFoundException if media with given ID doesn't exist
     * @throws com.imovel.api.exception.AuthorizationException if user is not authorized to delete
     */
    @DeleteMapping("/{mediaId}")
    public ResponseEntity<StandardResponse<Void>> deleteMedia(@PathVariable String mediaId) {
        // TODO: Implement delete media logic
        // Service layer would throw appropriate exceptions like:
        // - ResourceNotFoundException if media doesn't exist
        // - AuthorizationException if user can't delete this media
        return ResponseEntity.noContent().build();
    }
}