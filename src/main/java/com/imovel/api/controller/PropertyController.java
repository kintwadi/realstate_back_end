package com.imovel.api.controller;

import com.imovel.api.response.StandardResponse;
import org.springframework.web.bind.annotation.*;

/**
 * Property management endpoints
 */
@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    @GetMapping
    public StandardResponse<Void> getAllProperties() {
        // TODO: Implement get all properties logic
        return new StandardResponse<>();
    }

    @PostMapping
    public StandardResponse<Void> createProperty() {
        // TODO: Implement create property logic
        return new StandardResponse<>();
    }

    @GetMapping("/{id}")
    public StandardResponse<Void> getProperty(@PathVariable String id) {
        // TODO: Implement get property logic
        return new StandardResponse<>();
    }

    @PutMapping("/{id}")
    public StandardResponse<Void> updateProperty(@PathVariable String id) {
        // TODO: Implement update property logic
        return new StandardResponse<>();
    }

    @DeleteMapping("/{id}")
    public StandardResponse<Void> deleteProperty(@PathVariable String id) {
        // TODO: Implement delete property logic
        return new StandardResponse<>();
    }

    @GetMapping("/me")
    public StandardResponse<Void> getUserProperties() {
        // TODO: Implement get user properties logic
        return new StandardResponse<>();
    }

    @PostMapping("/{id}/media")
    public StandardResponse<Void> uploadPropertyMedia(@PathVariable String id) {
        // TODO: Implement upload property media logic
        return new StandardResponse<>();
    }

    @DeleteMapping("/{id}/media/{mediaId}")
    public StandardResponse<Void> deletePropertyMedia(
            @PathVariable String id,
            @PathVariable String mediaId) {
        // TODO: Implement delete property media logic
        return new StandardResponse<>();
    }
}