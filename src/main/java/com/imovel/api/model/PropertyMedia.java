package com.imovel.api.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "property_media")
public class PropertyMedia {

    @Id
    private String id;
    private String name;
    private String type;
    private Long size;
    private String url;
    private int width;
    private int height;
    private String format;
    private Instant uploadDate;
    private String description;
    private Long propertyId; // logical relation with property
    @Lob
    private byte[] rawData; // only used when selected  DatabaseStorageProvider

    // Constructors
    public PropertyMedia() {
    }

    public PropertyMedia(String name,
                         String type,
                         Long size,
                         String url) {
        this.name = name;
        this.type = type;
        this.size = size;
        this.url = url;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Instant getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Instant uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }
    
    public byte[] getRawData() {
        return rawData;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }
}
