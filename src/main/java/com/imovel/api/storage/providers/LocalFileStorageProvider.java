package com.imovel.api.storage.providers;

import com.imovel.api.storage.MediaMetadata;
import com.imovel.api.storage.StorageProvider;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public class LocalFileStorageProvider implements StorageProvider {
    private final Path basePath;
    private final String baseUrl;

    public LocalFileStorageProvider(String basePath, String baseUrl)  {
        this.basePath = Paths.get(basePath).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;
        try {
            Files.createDirectories(this.basePath); // Ensure base directory exists
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public String uploadFile(MultipartFile file, String fileName, Map<String, String> metadata) throws IOException {
        Path filePath = resolveSafePath(fileName);
        Files.createDirectories(filePath.getParent());
        
        // Save the file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Save metadata
        if (metadata != null && !metadata.isEmpty()) {
            Path metadataPath = filePath.resolveSibling(filePath.getFileName() + ".meta");
            Files.writeString(metadataPath, mapToJson(metadata));
        }
        
        return generateFileUrl(fileName);
    }

    @Override
    public void deleteFile(String fileName) throws IOException {
        Path filePath = resolveSafePath(fileName);
        Files.deleteIfExists(filePath);
        
        // Delete metadata file if exists
        Path metadataPath = filePath.resolveSibling(filePath.getFileName() + ".meta");
        Files.deleteIfExists(metadataPath);
    }

    @Override
    public MediaMetadata getFileMetadata(String fileName) throws IOException {
        Path filePath = resolveSafePath(fileName);
        if (!Files.exists(filePath)) {
            throw new NoSuchFileException("File not found: " + fileName);
        }

        BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
        Map<String, String> metadata = readMetadataFile(filePath);

        return new LocalFileMetadata(
            filePath,
            fileName,
            attrs,
            metadata
        );
    }

    @Override
    public List<String> listFiles(String prefix) throws IOException {
        Path startPath = resolveSafePath(prefix);
        
        if (!Files.exists(startPath)) {
            return Collections.emptyList();
        }

        try (Stream<Path> stream = Files.walk(startPath)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(path -> !path.getFileName().toString().endsWith(".meta"))
                .map(this.basePath::relativize)
                .map(Path::toString)
                .toList();
        }
    }

    private Path resolveSafePath(String fileName) throws IOException {
        Path resolvedPath = basePath.resolve(fileName).normalize();
        
        // Security check to prevent directory traversal
        if (!resolvedPath.startsWith(basePath)) {
            throw new IOException("Invalid path: attempted directory traversal");
        }
        
        return resolvedPath;
    }

    private String generateFileUrl(String fileName) {
        return baseUrl + "/" + fileName;
    }

    private Map<String, String> readMetadataFile(Path filePath) throws IOException {
        Path metadataPath = filePath.resolveSibling(filePath.getFileName() + ".meta");
        if (Files.exists(metadataPath)) {
            String content = Files.readString(metadataPath);
            return jsonToMap(content);
        }
        return Collections.emptyMap();
    }

    private String mapToJson(Map<String, String> map) {
        // Simple JSON serialization (replace with your preferred JSON library)
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (sb.length() > 1) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":\"")
              .append(entry.getValue().replace("\"", "\\\"")).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    private Map<String, String> jsonToMap(String json) {
        // Simple JSON parsing (replace with your preferred JSON library)
        Map<String, String> map = new HashMap<>();
        json = json.trim().replaceAll("[{}\"]", "");
        if (!json.isEmpty()) {
            for (String pair : json.split(",")) {
                String[] kv = pair.split(":", 2);
                if (kv.length == 2) {
                    map.put(kv[0].trim(), kv[1].trim());
                }
            }
        }
        return map;
    }

    private static class LocalFileMetadata implements MediaMetadata {
        private final Path filePath;
        private final String fileName;
        private final BasicFileAttributes attrs;
        private final Map<String, String> metadata;

        public LocalFileMetadata(Path filePath, String fileName, 
                               BasicFileAttributes attrs, Map<String, String> metadata) {
            this.filePath = filePath;
            this.fileName = fileName;
            this.attrs = attrs;
            this.metadata = metadata;
        }

        @Override
        public String getName() {
            return metadata.getOrDefault("original-filename", fileName);
        }

        @Override
        public String getType() {
            try {
                return Files.probeContentType(filePath);
            } catch (IOException e) {
                return "application/octet-stream";
            }
        }

        @Override
        public long getSize() {
            return attrs.size();
        }

        @Override
        public String getUrl() {
            return "file://" + filePath.toString();
        }

        @Override
        public Instant getUploadDate() {
            return attrs.creationTime().toInstant();
        }

        @Override
        public String getDescription() {
            return metadata.get("description");
        }

        @Override
        public Long getPropertyId() {
            String propertyId = metadata.get("property-id");
            return propertyId != null ? Long.valueOf(propertyId) : null;
        }
    }
}
