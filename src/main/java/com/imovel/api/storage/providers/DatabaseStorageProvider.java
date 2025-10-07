package com.imovel.api.storage.providers;

import com.imovel.api.model.PropertyMedia;
import com.imovel.api.repository.PropertyMediaRepository;
import com.imovel.api.storage.MediaMetadata;
import com.imovel.api.storage.StorageProvider;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DatabaseStorageProvider implements StorageProvider {
    private final PropertyMediaRepository propertyMediaRepository;

    public DatabaseStorageProvider(PropertyMediaRepository propertyMediaRepository) {
        this.propertyMediaRepository = propertyMediaRepository;
    }

    @Override
    public String uploadFile(MultipartFile file, String fileName, Map<String, String> metadata) throws IOException {
        PropertyMedia media = new PropertyMedia();
        media.setId(UUID.randomUUID().toString());
        media.setName(fileName);
        media.setType(file.getContentType());
        media.setSize(file.getSize());
        media.setUrl(fileName);
        media.setUploadDate(Instant.now());
        media.setDescription(metadata.get("description"));
        media.setPropertyId(metadata.get("property-id") != null ? 
            Long.valueOf(metadata.get("property-id")) : null);
        
        propertyMediaRepository.save(media);
        return media.getUrl();
    }

    @Override
    public void deleteFile(String fileName) throws IOException {
        propertyMediaRepository.deleteByName(fileName);
    }

    @Override
    public MediaMetadata getFileMetadata(String fileName) throws IOException {
        return propertyMediaRepository.findByName(fileName)
                .map(this::toFileMetadata)
                .orElseThrow(() -> new IOException("File not found"));
    }

    @Override
    public List<String> listFiles(String prefix) throws IOException {
        return propertyMediaRepository.findAllByNameStartingWith(prefix)
                .stream()
                .map(PropertyMedia::getName)
                .toList();
    }

    private MediaMetadata toFileMetadata(PropertyMedia media) {
        return new MediaMetadata() {
            @Override
            public String getName() {
                return media.getName();
            }

            @Override
            public String getType() {
                return media.getType();
            }

            @Override
            public long getSize() {
                return media.getSize();
            }

            @Override
            public String getUrl() {
                return media.getUrl();
            }

            @Override
            public Instant getUploadDate() {
                return media.getUploadDate();
            }

            @Override
            public String getDescription() {
                return media.getDescription();
            }

            @Override
            public Long getPropertyId() {
                return media.getPropertyId();
            }
        };
    }
}
