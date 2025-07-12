package com.imovel.api.controller;

import com.imovel.api.request.UserProfileUpdateRequestDto;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.UserProfileResponseDto;
import com.imovel.api.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling user profile related operations.
 * Provides endpoints for retrieving and updating user profiles.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @return ApplicationResponse with user profile or error
     */
    @GetMapping("/me")
    public ApplicationResponse<UserProfileResponseDto> getCurrentUserProfile() {
        return userService.getCurrentUserProfile();
    }

    /**
     * Updates the profile of the currently authenticated user.
     *
     * @param updateRequestDto DTO containing the updated profile information
     * @return ApplicationResponse with updated profile or error
     */
    @PutMapping("/profile-update")
    public ApplicationResponse<UserProfileResponseDto> updateUserProfile(
            @Valid @RequestBody UserProfileUpdateRequestDto updateRequestDto) {
        return userService.updateCurrentUserProfile(updateRequestDto);
    }
}