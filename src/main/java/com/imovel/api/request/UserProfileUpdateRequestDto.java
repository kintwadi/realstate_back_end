package com.imovel.api.request;

import jakarta.validation.constraints.Size;

import java.util.List;

public class UserProfileUpdateRequestDto {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(max = 20, message = "Phone number can be up to 20 characters")
    private String phone;

    @Size(max = 255, message = "Avatar URL can be up to 255 characters")
    private String avatar;

    private List<String> socialLinks;

    public UserProfileUpdateRequestDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public List<String> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(List<String> socialLinks) {
        this.socialLinks = socialLinks;
    }
}
