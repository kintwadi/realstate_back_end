package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.error.ErrorCode;
import com.imovel.api.model.PropertyMedia;
import com.imovel.api.repository.PropertyMediaRepository;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.PropertyMediaResponse;
import com.imovel.api.storage.MediaMetadata;
import com.imovel.api.storage.StorageProvider;
import com.imovel.api.storage.StorageProviderFactory;
import com.imovel.api.logger.ApiLogger;
import com.imovel.api.storage.StorageType;
import com.imovel.api.util.Util;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PropertyMediaService {


    private final StorageProviderFactory storageProviderFactory;
    private final PropertyMediaRepository propertyMediaRepository;

    @Value("${storage.type.provider}")
    private String storageType;
    @Autowired
    public PropertyMediaService(StorageProviderFactory storageProviderFactory,
                                PropertyMediaRepository propertyMediaRepository) {
        this.storageProviderFactory = storageProviderFactory;
        this.propertyMediaRepository = propertyMediaRepository;
    }

    public ApplicationResponse<PropertyMediaResponse> upload(MultipartFile file, Long propertyId,
                                                             String description) {
        try {
            UUID uuid = UUID.randomUUID();
            if (file == null || file.isEmpty()) {
                ApiLogger.error("PropertyMediaService.upload", "File cannot be empty");
                return errorResponse(ApiCode.INVALID_PAYLOAD, "File cannot be empty");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                ApiLogger.error("PropertyMediaService.upload", "Invalid file name");
                return errorResponse(ApiCode.INVALID_PAYLOAD, "Invalid file name");
            }

            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = (propertyId != null ? propertyId + "/" : "") + uuid + fileExtension;

            // Prepare metadata
            Map<String, String> metadata = new HashMap<>();
            if (description != null) metadata.put("description", description);
            metadata.put("original-filename", originalFilename);
            if (propertyId != null) metadata.put("property-id", propertyId.toString());

            StorageProvider storageProvider = storageProviderFactory.createStorageProvider();
            String fileUrl = storageProvider.uploadFile(file, fileName, metadata);

            // Create response
            PropertyMedia media = new PropertyMedia();
            media.setId(uuid.toString());
            media.setName(fileName);
            media.setType(file.getContentType());
            media.setSize(file.getSize());
            media.setUrl(fileUrl);
            media.setUploadDate(Instant.now());
            media.setDescription(description);
            media.setPropertyId(propertyId);
            media.setWidth(0); // Set default or extract from file if possible
            media.setHeight(0); // Set default or extract from file if possible
            media.setFormat(fileExtension.replace(".", ""));
            media.setRawData(file.getBytes());
            PropertyMediaResponse response = new PropertyMediaResponse(
                    media.getId(),
                    media.getName(),
                    media.getType(),
                    media.getSize(),
                    media.getUrl(),
                    media.getWidth(),
                    media.getHeight(),
                    media.getFormat(),
                    media.getUploadDate(),
                    media.getDescription(),
                    media.getPropertyId(),
                    Util.convertBytesToBase64WithMime(media.getRawData(),media.getType())
            );

            ApiLogger.info("PropertyMediaService.upload", "File uploaded successfully", response);
            return ApplicationResponse.success(response, "File uploaded successfully");

        } catch (IOException e) {
            ApiLogger.error("PropertyMediaService.upload", "Failed to upload file", e);
            return errorResponse(ApiCode.PROPERTY_IMAGE_UPLOAD_FAILED,
                    "Failed to upload file: " + e.getMessage());
        }
    }

    @Transactional
    public ApplicationResponse<List<PropertyMediaResponse>> getAll(Long propertyId) {
        try {
            List<PropertyMedia> propertyMediaList = propertyMediaRepository.findAllByPropertyId(propertyId);
            if (!propertyMediaList.isEmpty() && StorageType.DATABASE.name().equals(storageType)) {
                List<PropertyMediaResponse> responses = propertyMediaList.stream()
                        .map(media -> new PropertyMediaResponse(
                                media.getId(),
                                media.getName(),
                                media.getType(),
                                media.getSize(),
                                media.getUrl(),
                                media.getWidth(),
                                media.getHeight(),
                                media.getFormat(),
                                media.getUploadDate(),
                                media.getDescription(),
                                media.getPropertyId(),
                                Util.convertBytesToBase64WithMime(media.getRawData(),media.getType())
                        ))
                        .collect(Collectors.toList());

                ApiLogger.info("PropertyMediaService.getAll", "Retrieved media from database", responses.size());
                return ApplicationResponse.success(responses);
            }

            StorageProvider storageProvider = storageProviderFactory.createStorageProvider();
            List<String> fileNames = storageProvider.listFiles(propertyId != null ? propertyId + "/" : "");

            List<PropertyMediaResponse> responses = fileNames.stream()
                    .map(fileName -> {
                        try {
                            MediaMetadata metadata = storageProvider.getFileMetadata(fileName);
                            return new PropertyMediaResponse(
                                    UUID.randomUUID().toString(),
                                    metadata.getName(),
                                    metadata.getType(),
                                    metadata.getSize(),
                                    metadata.getUrl(),
                                    0,
                                    0,
                                    "",
                                    metadata.getUploadDate(),
                                    metadata.getDescription(),
                                    metadata.getPropertyId(),""
                            );
                        } catch (IOException e) {
                            ApiLogger.error("PropertyMediaService.getAll", "Failed to get metadata for file", fileName);
                            // Return a minimal response with available information
                            return new PropertyMediaResponse(
                                    UUID.randomUUID().toString(),
                                    fileName,
                                    "unknown",
                                    0,
                                    "",
                                    0,
                                    0,
                                    "",
                                    Instant.now(),
                                    "",
                                    propertyId,""
                            );
                        }
                    })
                    .collect(Collectors.toList());

            ApiLogger.info("PropertyMediaService.getAll", "Retrieved media from storage", responses.size());
            return ApplicationResponse.success(responses);
        } catch (Exception e) {
            ApiLogger.error("PropertyMediaService.getAll", "Failed to list media", e);
            return errorResponse(ApiCode.RESOURCE_NOT_FOUND,
                    "Failed to list media: " + e.getMessage());
        }
    }

    @Transactional
    public ApplicationResponse<PropertyMediaResponse> getPropertyMedia(Long propertyId, String id) {
        try {
            Optional<PropertyMedia> propertyMediaOptional = propertyMediaRepository.findByIdAndPropertyId(id,propertyId);
            if (propertyMediaOptional.isPresent() && StorageType.DATABASE.name().equals(storageType) )  {
                PropertyMedia media = propertyMediaOptional.get();
                PropertyMediaResponse response = new PropertyMediaResponse(
                        media.getId(),
                        media.getName(),
                        media.getType(),
                        media.getSize(),
                        media.getUrl(),
                        media.getWidth(),
                        media.getHeight(),
                        media.getFormat(),
                        media.getUploadDate(),
                        media.getDescription(),
                        media.getPropertyId(),
                        Util.convertBytesToBase64WithMime(media.getRawData(),media.getType())
                );

                ApiLogger.info("PropertyMediaService.getByName", "Retrieved media from database", response);
                return ApplicationResponse.success(response);
            }

            StorageProvider storageProvider = storageProviderFactory.createStorageProvider();
            MediaMetadata metadata = storageProvider.getFileMetadata(id);

            PropertyMediaResponse response = new PropertyMediaResponse(
                    UUID.randomUUID().toString(),
                    metadata.getName(),
                    metadata.getType(),
                    metadata.getSize(),
                    metadata.getUrl(),
                    0,
                    0,
                    "",
                    metadata.getUploadDate(),
                    metadata.getDescription(),
                    metadata.getPropertyId(),""
            );

            ApiLogger.info("PropertyMediaService.getByName", "Retrieved media from storage", response);
            return ApplicationResponse.success(response);
        } catch (Exception e) {
            ApiLogger.error("PropertyMediaService.getByName", "Media not found", id);
            return errorResponse(ApiCode.RESOURCE_NOT_FOUND,
                    "Media not found: " + e.getMessage());
        }
    }

    @Transactional
    public ApplicationResponse<PropertyMediaResponse> delete(Long propertyId,String id) {
        try {
            Optional<PropertyMedia> mediaOptional = propertyMediaRepository.findByIdAndPropertyId(id,propertyId);
            if (mediaOptional.isEmpty()) {
                ApiLogger.error("PropertyMediaService.delete", "Media not found in database", id);
                return errorResponse(ApiCode.RESOURCE_NOT_FOUND, "Media not found in database");
            }

            PropertyMedia media = mediaOptional.get();
            StorageProvider storageProvider = storageProviderFactory.createStorageProvider();
            storageProvider.deleteFile(id);

            propertyMediaRepository.delete(media);

            PropertyMediaResponse response = new PropertyMediaResponse(
                    media.getId(),
                    media.getName(),
                    media.getType(),
                    media.getSize(),
                    media.getUrl(),
                    media.getWidth(),
                    media.getHeight(),
                    media.getFormat(),
                    media.getUploadDate(),
                    media.getDescription(),
                    media.getPropertyId(),
                    Util.convertBytesToBase64WithMime(media.getRawData(),media.getType())
            );

            ApiLogger.info("PropertyMediaService.delete", "Media deleted successfully", response);
            return ApplicationResponse.success(response, "Media deleted successfully");
        } catch (Exception e) {
            ApiLogger.error("PropertyMediaService.delete", "Failed to delete media: "+id, e);
            return errorResponse(ApiCode.SYSTEM_ERROR,
                    "Failed to delete media: " + e.getMessage());
        }
    }

    private <T> ApplicationResponse<T> errorResponse(ApiCode code, String message) {
        ApiLogger.error("PropertyMediaService.errorResponse", message, code);
        return ApplicationResponse.error(
                new ErrorCode(code.getCode(), message, code.getHttpStatus())
        );
    }
}
