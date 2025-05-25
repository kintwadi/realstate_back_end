package com.imovel.api.model;

import com.imovel.api.model.enums.MediaType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "media")
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType type; 

    private String format; 
    private Integer width; 
    private Integer height; 
    private Long size; 
    private Integer duration; 
    private boolean isPrimary; 

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; 

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property; // Property "1" -- "0..*" Media

    // Constructors
    public Media() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Media(Long id, String url, MediaType type, Property property) {
        this();
        this.id = id;
        this.url = url;
        this.type = type;
        this.property = property;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public MediaType getType() { return type; }
    public void setType(MediaType type) { this.type = type; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Media media = (Media) o;
        return Objects.equals(id, media.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Media{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", type=" + type +
                '}';
    }
}