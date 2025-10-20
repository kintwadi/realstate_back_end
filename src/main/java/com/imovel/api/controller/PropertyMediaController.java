package com.imovel.api.controller;

import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.PropertyMediaResponse;
import com.imovel.api.services.PropertyMediaService;
import com.imovel.api.storage.StorageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/media")
public class PropertyMediaController {

    private final PropertyMediaService mediaService;
    @Value("${storage.type.provider}")
    private String storageType;

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

    @GetMapping("/all/{propertyId}")
    public ApplicationResponse<List<PropertyMediaResponse>> getAll(@PathVariable Long propertyId) {
        return mediaService.getAll(propertyId);
    }

    @GetMapping("/find-by-id/{propertyId}/{id}")
    public ApplicationResponse<PropertyMediaResponse> getPropertyMedia(@PathVariable Long propertyId,@PathVariable String id) {


       if(  StorageType.S3.name().equals(storageType))
       {
           return mediaService.getPropertyMedia(propertyId,propertyId + "/" + id);
       }
        return mediaService.getPropertyMedia(propertyId,id);
    }

    @DeleteMapping("remove/{propertyId}/{id}")
    public ApplicationResponse<PropertyMediaResponse> delete(@PathVariable Long propertyId,@PathVariable String id) {

        if(StorageType.S3.name().equals(storageType))
        {
            return mediaService.delete(propertyId,propertyId + "/" + id);
        }
        return mediaService.delete(propertyId,id);
    }

}
