package com.imovel.api.controller;

import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.PropertyMediaResponse;
import com.imovel.api.services.PropertyMediaService;
import com.imovel.api.storage.StorageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/media")
public class PropertyMediaController {

    private final PropertyMediaService mediaService;

    @Autowired
    public PropertyMediaController(PropertyMediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping("/upload")
    public ApplicationResponse<PropertyMediaResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("propertyId") Long propertyId,
            @RequestParam(value = "description", required = false) String description) {
        return mediaService.upload(file, propertyId, description);
    }

    @GetMapping("/property/{propertyId}")
    public ApplicationResponse<List<PropertyMediaResponse>> getAll(@PathVariable Long propertyId) {
        return mediaService.getAll(propertyId);
    }

    @GetMapping("{propertyId}/{name}")
    public ApplicationResponse<PropertyMediaResponse> getByName(@PathVariable Long propertyId,@PathVariable String name) {

        return mediaService.getByName(propertyId + "/" + name);
    }

    @DeleteMapping("{propertyId}/{name}")
    public ApplicationResponse<PropertyMediaResponse> delete(@PathVariable Long propertyId,@PathVariable String name) {
        return mediaService.delete(propertyId + "/" + name);
    }

}