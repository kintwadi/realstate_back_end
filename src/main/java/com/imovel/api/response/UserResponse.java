package com.imovel.api.response;

import com.imovel.api.model.Role;
import com.imovel.api.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String avatar;

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
    public static Optional<UserResponse> parse(User user){
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        return Optional.of(dto);
    }
}