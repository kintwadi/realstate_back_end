package com.imovel.api.controller;

import com.imovel.api.model.PropertyMedia;
import com.imovel.api.response.ApplicationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    @Autowired
    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping("/upload")
    public ApplicationResponse<PropertyMedia> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("propertyId") String propertyId,
            @RequestParam(value = "description", required = false) String description) {
        return mediaService.upload(file, propertyId, description);
    }

    @GetMapping("/property/{propertyId}")
    public ApplicationResponse<List<PropertyMedia>> getAll(@PathVariable String propertyId) {
        return mediaService.getAll(propertyId);
    }

    //http://localhost:8080/imovel/api/media/1/6dfd38a0-7f45-4c02-a0f1-29ab88b69a70.png
    @GetMapping("{propertyId}/{name}")
    public ApplicationResponse<PropertyMedia> getByName(@PathVariable Long propertyId,@PathVariable String name) {

        return mediaService.getByName(propertyId + "/" + name);
    }

    @DeleteMapping("{propertyId}/{name}")
    public ApplicationResponse<PropertyMedia> delete(@PathVariable Long propertyId,@PathVariable String name) {
        return mediaService.delete(propertyId + "/" + name);
    }

}