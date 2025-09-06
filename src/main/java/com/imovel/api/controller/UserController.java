package com.imovel.api.controller;

import com.imovel.api.request.UserUpdateRequest;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.UserResponse;
import com.imovel.api.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("")
    public ApplicationResponse <List<UserResponse>> getAllUsers(HttpSession session) {

        return userService.getAllUsers(session);
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @return ApplicationResponse with user profile or error
     */
    @GetMapping("/me")
    public ApplicationResponse<UserResponse> getCurrentUserProfile(HttpSession session) {


        return userService.getCurrentUserProfile(session);
    }

    /**
     * Updates the profile of the currently authenticated user.
     *
     * @param userRequest  containing the updated profile information
     * @return ApplicationResponse with updated profile or error
     */
    @PutMapping("/me")
    public ApplicationResponse<UserResponse> updateUserProfile(
            @Valid @RequestBody UserUpdateRequest userRequest, HttpSession session)
    {
        return userService.updateCurrentUser(userRequest,session);
    }

    @DeleteMapping("/me")
    public ApplicationResponse<UserResponse> deleteUserProfile(HttpSession session)
    {
        return userService.deleteCurrentUser(session);
    }


}