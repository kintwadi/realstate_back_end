package com.imovel.api.request; // Or your actual DTO package


public class PasswordChangeRequest {

    private String oldPassword;

    private String newPassword;
    private String email;

    public PasswordChangeRequest() {
    }

    public PasswordChangeRequest(String oldPassword, String newPassword, String userEmail) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.email = userEmail;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
