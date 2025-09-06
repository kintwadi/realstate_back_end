package com.imovel.api.request;

import java.util.List;

public class UserUpdateRequest {

    private String name;

    private String phone;

    private String avatar;

    private List<String> socialLinks;

    public UserUpdateRequest() {
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
