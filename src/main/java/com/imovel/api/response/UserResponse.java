package com.imovel.api.response;

import com.imovel.api.model.User;
import com.imovel.api.request.SocialLinkDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserResponse implements Serializable {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private List<SocialLinkDto> socialLinks;

    public UserResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public static Optional<UserResponse> parse(User user) {
        if (user == null) return Optional.empty();

        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setName(user.getName());
        resp.setEmail(user.getEmail());
        resp.setPhone(user.getPhone());
        resp.setAvatar(user.getAvatar());

        if (user.getSocialLinks() != null) {
            resp.setSocialLinks(
                    user.getSocialLinks().stream()
                            .map(sl -> {
                                SocialLinkDto dto = new SocialLinkDto();
                                dto.setPlatform(sl.getPlatform());
                                dto.setUrl(sl.getUrl());
                                return dto;
                            })
                            .collect(Collectors.toList())
            );
        }
        return Optional.of(resp);
    }

    public static List<UserResponse> parse(List<User> users) {
        List<UserResponse> currentUsers = new ArrayList<>();

        for (User user : users) {
            UserResponse dto = new UserResponse();
            dto.setId(user.getId());
            dto.setName(user.getName());
            dto.setEmail(user.getEmail());
            dto.setPhone(user.getPhone());
            dto.setAvatar(user.getAvatar());
            currentUsers.add(dto);
        }
        return currentUsers;
    }

    public List<SocialLinkDto> getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(List<SocialLinkDto> socialLinks) {
        this.socialLinks = socialLinks;
    }
}