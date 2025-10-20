package com.imovel.api.storage.providers;

import com.imovel.api.storage.MediaMetadata;
import com.imovel.api.storage.StorageProvider;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class S3StorageProvider implements StorageProvider {
    private final S3Client s3Client;
    private static  String bucketName;
    private static final String S3_URL_FORMAT = "https://%s.s3.amazonaws.com/%s";

    public S3StorageProvider(S3Client s3Client, String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public String uploadFile(MultipartFile file, String fileName, Map<String, String> metadata) throws IOException {
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .contentType(file.getContentType())
                        .metadata(metadata)
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        return generateUrl(fileName);
    }

    @Override
    public void deleteFile(String fileName) throws IOException {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build());
    }

    @Override
    public MediaMetadata getFileMetadata(String fileName) throws IOException {
        HeadObjectResponse headObject = s3Client.headObject(HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build());

        return new S3FileMetadata(headObject, fileName);
    }

    @Override
    public List<String> listFiles(String prefix) throws IOException {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        return s3Client.listObjectsV2(request).contents().stream()
                .filter(s3Object -> !s3Object.key().endsWith("/"))
                .map(S3Object::key)
                .toList();
    }

    private String generateUrl(String fileName) {
        return String.format(S3_URL_FORMAT, bucketName, fileName);
    }

    private static class S3FileMetadata implements MediaMetadata {
        private final HeadObjectResponse headObject;
        private final String fileName;

        public S3FileMetadata(HeadObjectResponse headObject, String fileName) {
            this.headObject = headObject;
            this.fileName = fileName;
        }

        @Override
        public String getName() {
            return headObject.metadata().get("original-filename");
        }

        @Override
        public String getType() {
            return headObject.contentType();
        }

        @Override
        public long getSize() {
            return headObject.contentLength();
        }

        @Override
        public String getUrl() {
            return String.format(S3_URL_FORMAT,S3StorageProvider.bucketName, fileName);
        }

        @Override
        public Instant getUploadDate() {
            return headObject.lastModified();
        }

        @Override
        public String getDescription() {
            return headObject.metadata().get("description");
        }

        @Override
        public Long getPropertyId() {
            String propertyId = headObject.metadata().get("property-id");
            return propertyId != null ? Long.valueOf(propertyId) : null;
        }
    }

}
