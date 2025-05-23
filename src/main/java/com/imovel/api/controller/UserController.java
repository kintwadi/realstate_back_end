package com.imovel.api.controller;

import com.imovel.api.response.StandardResponse;
import org.springframework.web.bind.annotation.*;

/**
 * User profile management endpoints
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public StandardResponse<Void> getCurrentUser() {
        // TODO: Implement get current user logic
        return new StandardResponse<>();
    }

    @PutMapping("/me")
    public StandardResponse<Void> updateProfile() {
        // TODO: Implement update profile logic
        return new StandardResponse<>();
    }

    @PutMapping("/me/password")
    public StandardResponse<Void> changePassword() {
        // TODO: Implement change password logic
        return new StandardResponse<>();
    }

    @PostMapping("/me/avatar")
    public StandardResponse<Void> uploadAvatar() {
        // TODO: Implement upload avatar logic
        return new StandardResponse<>();
    }

    @DeleteMapping("/me/avatar")
    public StandardResponse<Void> removeAvatar() {
        // TODO: Implement remove avatar logic
        return new StandardResponse<>();
    }
}