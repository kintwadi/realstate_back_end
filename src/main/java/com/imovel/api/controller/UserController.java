package com.imovel.api.controller;

import com.imovel.api.request.UserProfileUpdateRequestDto;
import com.imovel.api.response.StandardResponse;
import com.imovel.api.response.UserProfileResponseDto;
import com.imovel.api.services.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<StandardResponse<UserProfileResponseDto>> getCurrentUserProfile() {
        try {
            UserProfileResponseDto userProfile = userService.getCurrentUserProfile();
            return new ResponseEntity<>(
                    new StandardResponse<>("User profile retrieved successfully.", null, userProfile),
                    HttpStatus.OK);
        } catch (RuntimeException e) {
            String errorCode = "USER_PROFILE_ERROR";
            if (e.getMessage() != null && e.getMessage().contains("Default user for profile operations")) {
                errorCode = "DEFAULT_USER_NOT_FOUND";
            }
            return new ResponseEntity<>(
                    new StandardResponse<>(e.getMessage(), errorCode, null),
                    HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/profile-update")
    public ResponseEntity<StandardResponse<UserProfileResponseDto>> updateUserProfile(
            @Valid @RequestBody UserProfileUpdateRequestDto updateRequestDto) {
        try {
            UserProfileResponseDto updatedUserProfile = userService.updateCurrentUserProfile(updateRequestDto);
            return new ResponseEntity<>(
                    new StandardResponse<>("User profile updated successfully.", null, updatedUserProfile),
                    HttpStatus.OK);
        } catch (RuntimeException e) {
            String errorCode = "USER_PROFILE_UPDATE_ERROR";
            if (e.getMessage() != null && e.getMessage().contains("Default user for profile operations")) {
                errorCode = "DEFAULT_USER_NOT_FOUND";
            }
            return new ResponseEntity<>(
                    new StandardResponse<>(e.getMessage(), errorCode, null),
                    HttpStatus.BAD_REQUEST);
        }
    }
}
