package com.imovel.api.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface StorageProvider {
    String uploadFile(MultipartFile file, String fileName, Map<String, String> metadata) throws IOException;
    void deleteFile(String fileName) throws IOException;
    MediaMetadata getFileMetadata(String fileName) throws IOException;
    List<String> listFiles(String prefix) throws IOException;
}
