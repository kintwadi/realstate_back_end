package com.imovel.api.services;

import com.imovel.api.model.User;
import com.imovel.api.model.enums.UserRole;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.request.PasswordChangeRequest;
import com.imovel.api.request.UserRegistrationRequest;
import com.imovel.api.response.StandardResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {


    private final UserRepository userRepository;
    @Autowired
    private AuthDetailsService authDetailsService;

    @Autowired
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> registerUser(UserRegistrationRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return Optional.empty();
        }

        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPhone(request.getPhone());
        newUser.setRole(UserRole.CLIENT);

        return Optional.of(userRepository.save(newUser));
    }

    public Optional<User> findByEmail(final String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> loginUser(String email, String password) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    /**
     * Changes the user's password after validating the request.
     * @see com.imovel.api.security.aspect.AuthServiceAspect will:
     *  1. Validate old password matches
     *  2. Update to new password
     *  3. Save the updated user entity
     * @param changePasswordRequestDto The DTO containing password change details (email, old password, new password)
     * @return StandardResponse indicating the result of the password change operation
     *
     */
    public StandardResponse changeUserPassword(PasswordChangeRequest changePasswordRequestDto) {
        // Initialize response with failure state (will be updated if successful)
        StandardResponse standardResponse = new StandardResponse(
                "PASSWORD_RESET_0001",
                "Old password does not match",
                null
        );
        // Find user by email from the repository
        userRepository.findByEmail(changePasswordRequestDto.getEmail())
                .ifPresent(user -> {
                    // Update response for successful case
                    standardResponse.setErrorCode("PASSWORD_RESET_000");
                    standardResponse.setErrorText("Password changed successfully");
                });

        return standardResponse;
    }


}