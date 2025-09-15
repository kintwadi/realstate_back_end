package com.imovel.api.model.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class SocialLink {

    @Column(name = "platform", nullable = false, length = 50)
    private String platform;

    @Column(name = "url", nullable = false, length = 512)
    private String url;

    public SocialLink() {
    }

    public SocialLink(String platform, String url) {
        this.platform = platform;
        this.url = url;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SocialLink that)) return false;
        return Objects.equals(platform, that.platform) && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform, url);
    }
}
