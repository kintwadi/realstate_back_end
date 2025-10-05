package com.imovel.api.response;

import com.imovel.api.model.PropertyMedia;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class PropertyMediaResponse {
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
    private Long propertyId;

    // Constructors
    public PropertyMediaResponse() {
    }

    public PropertyMediaResponse(String id, String name, String type, long size, String url, 
                               int width, int height, String format, Instant uploadDate, 
                               String description, Long propertyId) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.size = size;
        this.url = url;
        this.width = width;
        this.height = height;
        this.format = format;
        this.uploadDate = uploadDate;
        this.description = description;
        this.propertyId = propertyId;
    }



    // Static parsing methods
    public static PropertyMediaResponse parse(PropertyMedia propertyMedia) {
        if (propertyMedia == null) {
            return null;
        }
        
        return new PropertyMediaResponse(
            propertyMedia.getId(),
            propertyMedia.getName(),
            propertyMedia.getType(),
            propertyMedia.getSize(),
            propertyMedia.getUrl(),
            propertyMedia.getWidth(),
            propertyMedia.getHeight(),
            propertyMedia.getFormat(),
            propertyMedia.getUploadDate(),
            propertyMedia.getDescription(),
            propertyMedia.getPropertyId()
        );
    }

    public static List<PropertyMediaResponse> parse(List<PropertyMedia> propertyMediaList) {
        if (propertyMediaList == null) {
            return null;
        }
        
        return propertyMediaList.stream()
                .map(PropertyMediaResponse::parse)
                .collect(Collectors.toList());
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
}
