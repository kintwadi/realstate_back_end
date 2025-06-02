package com.imovel.api.controller;

import com.imovel.api.request.UserProfileUpdateRequestDto;
import com.imovel.api.response.StandardResponse;
import com.imovel.api.response.UserProfileResponseDto;
import com.imovel.api.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
     * @return ResponseEntity containing StandardResponse with user profile or error
     */
    @GetMapping("/me")
    public ResponseEntity<StandardResponse<UserProfileResponseDto>> getCurrentUserProfile() {
        StandardResponse<UserProfileResponseDto> response = userService.getCurrentUserProfile();
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the profile of the currently authenticated user.
     *
     * @param updateRequestDto DTO containing the updated profile information
     * @return ResponseEntity containing StandardResponse with updated profile or error
     */
    @PutMapping("/profile-update")
    public ResponseEntity<StandardResponse<UserProfileResponseDto>> updateUserProfile(
            @Valid @RequestBody UserProfileUpdateRequestDto updateRequestDto) {
        StandardResponse<UserProfileResponseDto> response = userService.updateCurrentUserProfile(updateRequestDto);
        return ResponseEntity.ok(response);
    }
}