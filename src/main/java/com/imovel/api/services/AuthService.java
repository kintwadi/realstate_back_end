package com.imovel.api.services;

import com.imovel.api.error.ApiCode;
import com.imovel.api.exception.ConflictException;
import com.imovel.api.model.Role;
import com.imovel.api.model.User;
import com.imovel.api.model.enums.RoleReference;
import com.imovel.api.repository.RoleRepository;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.request.PasswordChangeRequest;
import com.imovel.api.request.UserRegistrationRequest;
import com.imovel.api.response.ApplicationResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for handling authentication-related operations.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthDetailsService authDetailsService;

    @Autowired
    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       AuthDetailsService authDetailsService)
    {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.authDetailsService = authDetailsService;
    }

    /**
     * Registers a new user in the system.
     *
     * @param request The user registration request
     * @return StandardResponse containing the registered user
     * @throws ConflictException if email is already registered
     */
    public ApplicationResponse<User> registerUser(UserRegistrationRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException(ApiCode.INVALID_EMAIL_ALREADY_EXIST.getCode(),ApiCode.INVALID_EMAIL_ALREADY_EXIST.getMessage());
        }

        Role role = new Role();
        role.setRoleName(RoleReference.TENANT.name());
        Optional<Role> roleOptional = roleRepository.findByRoleName(RoleReference.TENANT.name());
        if(!roleOptional.isPresent()){
            roleRepository.save(role);
        }else{
            role = roleOptional.get();
        }


        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPhone(request.getPhone());
        newUser.setRole(role);

        User savedUser = userRepository.save(newUser);
        return ApplicationResponse.success(savedUser);
    }

    /**
     * Finds a user by email.
     *
     * @param email The email to search for
     * @return StandardResponse containing the user if found
     */
    public Optional<User> findByEmail(final String email) {

        return userRepository.findByEmail(email);

    }
    /**
     * Attempts to log in a user.
     *
     * @param email The user's email
     * @param password The user's password
     * @return StandardResponse containing the user if credentials are valid
     */
    public ApplicationResponse<User> loginUser(String email, String password) {

        return findByEmail(email)
                .map(ApplicationResponse::success)
                .orElse(ApplicationResponse.error(ApiCode.USER_NOT_FOUND.getCode(), ApiCode.USER_NOT_FOUND.getMessage() + email, HttpStatus.NOT_FOUND));

    }

    /**
     * Changes the user's password after validating the request.
     *
     * @param changePasswordRequestDto The DTO containing password change details
     * @return StandardResponse indicating the result of the operation
     */
    @Transactional
    public ApplicationResponse<User> changeUserPassword(PasswordChangeRequest changePasswordRequestDto) {
        return userRepository.findByEmail(changePasswordRequestDto.getEmail())
                .map(user -> ApplicationResponse.success(user, "Password changed successfully"))
                .orElse(ApplicationResponse.error(ApiCode.USER_NOT_FOUND.getCode(), ApiCode.USER_NOT_FOUND.getMessage(),HttpStatus.NOT_FOUND));
    }
}