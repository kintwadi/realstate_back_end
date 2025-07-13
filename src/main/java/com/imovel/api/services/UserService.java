package com.imovel.api.services;

import com.imovel.api.exception.ResourceNotFoundException;
import com.imovel.api.model.User;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.request.UserProfileUpdateRequestDto;
import com.imovel.api.response.ApplicationResponse;
import com.imovel.api.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

/**
 * Service class for handling user profile operations.
 * Manages user profile retrieval and updates with proper error handling.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    // Temporary constant for development - to be replaced with actual authenticated user ID
    private static final Long CURRENT_USER_ID_FOR_PROFILE_OPERATIONS = 1L;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the current authenticated user for profile operations.
     * Note: In production, this should get the user from security context.
     *
     * @return The authenticated user entity
     * @throws ResourceNotFoundException if the default user is not found
     */
    private User getCurrentAuthenticatedUser() {
        return userRepository.findById(CURRENT_USER_ID_FOR_PROFILE_OPERATIONS)
                .orElseThrow(() -> new ResourceNotFoundException("User", CURRENT_USER_ID_FOR_PROFILE_OPERATIONS));
    }

    /**
     * Retrieves the profile of the current authenticated user.
     *
     * @return StandardResponse containing the user profile DTO
     */
    @Transactional(readOnly = true)
    public ApplicationResponse<UserResponse> getCurrentUserProfile() {
        User currentUser = getCurrentAuthenticatedUser();
        return ApplicationResponse.success(mapToUserProfileResponseDto(currentUser));
    }

    /**
     * Updates the profile of the current authenticated user.
     *
     * @param updateRequestDto DTO containing the updated profile information
     * @return StandardResponse containing the updated user profile DTO
     */
    @Transactional
    public ApplicationResponse<UserResponse> updateCurrentUserProfile(UserProfileUpdateRequestDto updateRequestDto) {
        User currentUser = getCurrentAuthenticatedUser();

        // Update user fields if they are provided in the request
        if (updateRequestDto.getName() != null) {
            currentUser.setName(updateRequestDto.getName());
        }
        if (updateRequestDto.getPhone() != null) {
            currentUser.setPhone(updateRequestDto.getPhone());
        }
        if (updateRequestDto.getAvatar() != null) {
            currentUser.setAvatar(updateRequestDto.getAvatar());
        }
        if (updateRequestDto.getSocialLinks() != null) {
            currentUser.setSocialLinks(new ArrayList<>(updateRequestDto.getSocialLinks()));
        }

        User updatedUser = userRepository.save(currentUser);
        return ApplicationResponse.success(mapToUserProfileResponseDto(updatedUser));
    }

    /**
     * Maps a User entity to a UserProfileResponseDto.
     *
     * @param user The user entity to map
     * @return The mapped UserProfileResponseDto
     */
    private UserResponse mapToUserProfileResponseDto(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        dto.setRole(user.getRole());
        dto.setSocialLinks(user.getSocialLinks() != null ?
                new ArrayList<>(user.getSocialLinks()) : new ArrayList<>());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}