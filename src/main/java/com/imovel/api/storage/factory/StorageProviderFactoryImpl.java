package com.imovel.api.storage.factory;

import com.imovel.api.repository.PropertyMediaRepository;
import com.imovel.api.storage.StorageProvider;
import com.imovel.api.storage.StorageProviderFactory;
import com.imovel.api.storage.StorageType;
import com.imovel.api.storage.providers.DatabaseStorageProvider;
import com.imovel.api.storage.providers.LocalFileStorageProvider;
import com.imovel.api.storage.providers.S3StorageProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;



@Service
public class StorageProviderFactoryImpl implements StorageProviderFactory {
    private final S3Client s3Client;
    private final PropertyMediaRepository propertyMediaRepository;
    private final String bucketName;
    private final String localStoragePath;
    private final String localStorageUrl;
    private final String storageType;

    @Autowired
    public StorageProviderFactoryImpl(
            S3Client s3Client,
            PropertyMediaRepository propertyMediaRepository,
            @Value("${aws.s3.bucket-name}") String bucketName,
            @Value("${storage.local.path:/tmp/uploads}") String localStoragePath,
            @Value("${storage.local.url:http://localhost/files}") String localStorageUrl,
            @Value("${storage.type.provider}") String storageType) {
        this.s3Client = s3Client;
        this.propertyMediaRepository = propertyMediaRepository;
        this.bucketName = bucketName;
        this.localStoragePath = localStoragePath;
        this.localStorageUrl = localStorageUrl;
        this.storageType = storageType;
    }

    @Override
    public StorageProvider createStorageProvider() {
        return switch (StorageType.getValue(storageType)) {
            case S3 -> new S3StorageProvider(s3Client, bucketName);
            case DATABASE -> new DatabaseStorageProvider(propertyMediaRepository);
            case LOCAL -> new LocalFileStorageProvider(localStoragePath, localStorageUrl);
            default -> throw new IllegalArgumentException("Unsupported storage type: " + storageType);
        };
    }
}

