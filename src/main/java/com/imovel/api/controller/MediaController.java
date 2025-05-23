package com.imovel.api.controller;

import com.imovel.api.response.StandardResponse;
import org.springframework.web.bind.annotation.*;

/**
 * Media management endpoints
 */
@RestController
@RequestMapping("/api/media")
public class MediaController {

    @PostMapping("/upload")
    public StandardResponse<Void> uploadMedia() {
        // TODO: Implement upload media logic
        return new StandardResponse<>();
    }

    @GetMapping("/{mediaId}")
    public StandardResponse<Void> getMedia(@PathVariable String mediaId) {
        // TODO: Implement get media logic
        return new StandardResponse<>();
    }

    @DeleteMapping("/{mediaId}")
    public StandardResponse<Void> deleteMedia(@PathVariable String mediaId) {
        // TODO: Implement delete media logic
        return new StandardResponse<>();
    }
}