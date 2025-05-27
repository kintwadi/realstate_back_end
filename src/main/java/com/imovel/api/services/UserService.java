package com.imovel.api.services;

import com.imovel.api.model.User;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.request.UserProfileUpdateRequestDto;
import com.imovel.api.response.UserProfileResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
public class UserService {

    private final UserRepository userRepository;

    private static final Long CURRENT_USER_ID_FOR_PROFILE_OPERATIONS = 1L;

    @Autowired
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    private User getCurrentAuthenticatedUser() {
        return userRepository.findById(CURRENT_USER_ID_FOR_PROFILE_OPERATIONS)
                .orElseThrow(() -> new RuntimeException("Default user for profile operations (ID " +
                        CURRENT_USER_ID_FOR_PROFILE_OPERATIONS + ") not found. Ensure this user exists."));
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto getCurrentUserProfile() {
        User currentUser = getCurrentAuthenticatedUser();
        return mapToUserProfileResponseDto(currentUser);
    }

    @Transactional
    public UserProfileResponseDto updateCurrentUserProfile(UserProfileUpdateRequestDto updateRequestDto) {
        User currentUser = getCurrentAuthenticatedUser();

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
        return mapToUserProfileResponseDto(updatedUser);
    }

    private UserProfileResponseDto mapToUserProfileResponseDto(User user) {
        UserProfileResponseDto dto = new UserProfileResponseDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        dto.setRole(user.getRole());
        if (user.getSocialLinks() != null) {
            dto.setSocialLinks(new ArrayList<>(user.getSocialLinks()));
        } else {
            dto.setSocialLinks(new ArrayList<>());
        }
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
