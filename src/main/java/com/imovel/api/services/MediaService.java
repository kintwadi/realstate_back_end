package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.error.ErrorCode;
import com.imovel.api.model.PropertyMedia;
import com.imovel.api.response.ApplicationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service
public class MediaService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Autowired
    public MediaService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public ApplicationResponse<PropertyMedia> upload(MultipartFile file, String propertyId, String description) {
        try {
            if (file == null || file.isEmpty()) {
                return errorResponse(ApiCode.INVALID_PAYLOAD, "File cannot be empty");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                return errorResponse(ApiCode.INVALID_PAYLOAD, "Invalid file name");
            }

            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));

            String fileName = (propertyId != null ? propertyId + "/" : "") + UUID.randomUUID() + fileExtension;

            // Upload with metadata
            Map<String, String> metadata = new HashMap<>();
            if (description != null) metadata.put("description", description);
            metadata.put("original-filename", originalFilename);
            if (propertyId != null) metadata.put("property-id", propertyId);

            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .contentType(file.getContentType())
                            .metadata(metadata)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Create response
            PropertyMedia media = new PropertyMedia();
            media.setId(UUID.randomUUID().toString());
            media.setName(originalFilename);
            media.setType(file.getContentType());
            media.setSize(file.getSize());
            media.setUrl(generateUrl(fileName));
            media.setUploadDate(Instant.now());
            media.setDescription(description);
            media.setPropertyId(propertyId);

            return ApplicationResponse.success(media, "File uploaded successfully");

        } catch (IOException | S3Exception e) {
            return errorResponse(ApiCode.PROPERTY_IMAGE_UPLOAD_FAILED,
                    "Failed to upload file: " + e.getMessage());
        }
    }

    public ApplicationResponse<List<PropertyMedia>> getAll(String propertyId) {
        try {
            ListObjectsV2Request request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(propertyId != null ? propertyId + "/" : "")
                    .build();

            List<PropertyMedia> mediaList = new ArrayList<>();

            s3Client.listObjectsV2(request).contents().forEach(s3Object -> {
                if (!s3Object.key().endsWith("/")) {
                    mediaList.add(getMediaDetails(s3Object.key()));
                }
            });

            return ApplicationResponse.success(mediaList);
        } catch (S3Exception e) {
            return errorResponse(ApiCode.RESOURCE_NOT_FOUND,
                    "Failed to list media: " + e.getMessage());
        }
    }

    public ApplicationResponse<PropertyMedia> getByName(String name) {
        try {
            return ApplicationResponse.success(getMediaDetails(name));
        } catch (S3Exception e) {
            return errorResponse(ApiCode.RESOURCE_NOT_FOUND,
                    "Media not found: " + e.getMessage());
        }
    }

    public ApplicationResponse<PropertyMedia> delete(String name) {
        try {
            PropertyMedia media = getMediaDetails(name);
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(name)
                    .build());
            return ApplicationResponse.success(media, "Media deleted successfully");
        } catch (S3Exception e) {
            return errorResponse(ApiCode.SYSTEM_ERROR,
                    "Failed to delete media: " + e.getMessage());
        }
    }

    private PropertyMedia getMediaDetails(String key) throws S3Exception {
        HeadObjectResponse headObject = s3Client.headObject(HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());

        PropertyMedia media = new PropertyMedia();
        media.setName(headObject.metadata().get("original-filename"));
        media.setType(headObject.contentType());
        media.setSize(headObject.contentLength());
        media.setUrl(generateUrl(key));
        media.setUploadDate(headObject.lastModified());
        media.setDescription(headObject.metadata().get("description"));
        media.setPropertyId(headObject.metadata().get("property-id"));
        return media;
    }

    private String generateUrl(String fileName) {
        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);
    }

    private <T> ApplicationResponse<T> errorResponse(ApiCode code, String message) {
        return ApplicationResponse.error(
                new ErrorCode(code.getCode(), message, code.getHttpStatus())
        );
    }
}