package com.imovel.api.request;

public class ResetPasswordRequest {
    private String email;
    private String code;        // 5-digit code
    private String newPassword; // plaintext; will be encoded server-side

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
