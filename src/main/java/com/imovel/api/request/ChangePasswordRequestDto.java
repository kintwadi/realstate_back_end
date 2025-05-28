package com.imovel.api.request; // Or your actual DTO package


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequestDto {

    private String oldPassword;

    private String newPassword;
    private String userEmail;

    public ChangePasswordRequestDto() {
    }

    public ChangePasswordRequestDto(String oldPassword, String newPassword, String userEmail) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.userEmail = userEmail;
    }

    // Getters and Setters
    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

}