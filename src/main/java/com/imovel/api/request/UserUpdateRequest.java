package com.imovel.api.request;

import java.util.List;

public class UserUpdateRequest {

    private String name;

    private String phone;

    private String avatar;

    private List<SocialLinkDto> socialLinks;

    public UserUpdateRequest() {
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public List<SocialLinkDto> getSocialLinks() {
        return socialLinks;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setSocialLinks(List<SocialLinkDto> socialLinks) {
        this.socialLinks = socialLinks;
    }

}
