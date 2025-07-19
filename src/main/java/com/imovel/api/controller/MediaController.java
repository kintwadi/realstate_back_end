package com.imovel.api.controller;

import com.imovel.api.exception.ResourceNotFoundException;
import com.imovel.api.response.ApplicationResponse;
import org.springframework.web.bind.annotation.*;

/**
 * Media management endpoints
 *
 * All endpoints return ApplicationResponse for consistent response structure.
 * Exceptions are automatically handled by the GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/api/media")
public class MediaController {

    /**
     * Uploads media to the system
     *
     * @return ApplicationResponse with success status
     * @throws com.imovel.api.exception.ValidationException if upload request is invalid
     * @throws com.imovel.api.exception.ConflictException if media already exists
     */
    @PostMapping("/upload")
    public ApplicationResponse<Void> uploadMedia() {
        // TODO: Implement upload media logic
        // Service layer would throw appropriate exceptions like:
        // - ValidationException for invalid requests
        // - ConflictException if media already exists
        // - ResourceNotFoundException if referenced resources don't exist

        return ApplicationResponse.success(null);
    }

    /**
     * Retrieves media by its unique identifier
     *
     * @param mediaId The ID of the media to retrieve
     * @return ApplicationResponse with media data
     * @throws ResourceNotFoundException if media with given ID doesn't exist
     */
    @GetMapping("/{mediaId}")
    public ApplicationResponse<String> getMedia(@PathVariable String mediaId) {
        // TODO: Implement get media logic
        // Example service call that might throw:
        // Media media = mediaService.getMediaById(mediaId);
        // return ApplicationResponse.success(media.getUrl());
        return ApplicationResponse.success(null);
    }

    /**
     * Deletes media by its unique identifier
     *
     * @param mediaId The ID of the media to delete
     * @return ApplicationResponse with no content on successful deletion
     * @throws ResourceNotFoundException if media with given ID doesn't exist
     * @throws com.imovel.api.exception.AuthorizationException if user is not authorized to delete
     */
    @DeleteMapping("/{mediaId}")
    public ApplicationResponse<Void> deleteMedia(@PathVariable String mediaId) {
        // TODO: Implement delete media logic
        // Service layer would throw appropriate exceptions like:
        // - ResourceNotFoundException if media doesn't exist
        // - AuthorizationException if user can't delete this media
        return ApplicationResponse.success(null);
    }
}