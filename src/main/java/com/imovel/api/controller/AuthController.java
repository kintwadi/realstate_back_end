package com.imovel.api.controller;

import com.imovel.api.request.ChangePasswordRequestDto;
import com.imovel.api.request.UserLoginRequest;
import com.imovel.api.request.UserRegistrationRequest;
import com.imovel.api.response.StandardResponse;
import com.imovel.api.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService userService;

    @Autowired
    public AuthController(AuthService userService) {
        this.userService = userService;
    }

    @PostMapping("/register") //
    public ResponseEntity<StandardResponse<Object>> register(@RequestBody UserRegistrationRequest request) {
        if (request.getEmail() == null || request.getEmail().isEmpty() ||
                request.getPassword() == null || request.getPassword().isEmpty() ||
                request.getName() == null || request.getName().isEmpty()) {
            return new ResponseEntity<>(
                    new StandardResponse<>("Missing required fields", "REGISTRATION_001", null),
                    HttpStatus.BAD_REQUEST);
        }

        return userService.registerUser(request)
                .map(user -> new ResponseEntity<>(
                        new StandardResponse<>("User registered successfully", null, null),
                        HttpStatus.CREATED))
                .orElse(new ResponseEntity<>(
                        new StandardResponse<>("Email already registered", "REGISTRATION_002", null),
                        HttpStatus.CONFLICT));
    }

    @PostMapping("/login") //
    public ResponseEntity<StandardResponse<String>> login(@RequestBody UserLoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isEmpty() ||
                request.getPassword() == null || request.getPassword().isEmpty()) {
            return new ResponseEntity<>(
                    new StandardResponse<>("Email and password are required", "LOGIN_001", null),
                    HttpStatus.BAD_REQUEST);
        }

        return userService.loginUser(request.getEmail(), request.getPassword())
                .map(user -> new ResponseEntity<>(
                        new StandardResponse<>("Login successful", null, "Welcome " + user.getName()),
                        HttpStatus.OK))
                .orElse(new ResponseEntity<>(
                        new StandardResponse<>("Invalid email or password", "LOGIN_002", null),
                        HttpStatus.UNAUTHORIZED));
    }

    @PostMapping("/logout")
    public StandardResponse<Void> logout() {
        // TODO: Implement logout logic
        return new StandardResponse<>();
    }

    @PostMapping("/forgot-password")
    public StandardResponse<Void> forgotPassword() {
        // TODO: Implement forgot password logic
        return new StandardResponse<>();
    }

    @PostMapping("/reset-password") // Endpoint name as requested
    public ResponseEntity<StandardResponse<Object>> resetPassword(@Valid @RequestBody ChangePasswordRequestDto changePasswordRequestDto) {
        String errorCode = userService.changeUserPassword(changePasswordRequestDto);

        if (errorCode == null) {
            return new ResponseEntity<>(
                    new StandardResponse<>("Password changed successfully.", null, null),
                    HttpStatus.OK);
        } else {
            String message;
            HttpStatus status = switch (errorCode) {
                case "OLD_PASSWORD_MISMATCH" -> {
                    message = "Failed to change password. The old password provided is incorrect.";
                    yield HttpStatus.BAD_REQUEST;
                }
                case "USER_NOT_FOUND" -> {
                    message = "User not found. Authentication error.";
                    yield HttpStatus.NOT_FOUND;
                }
                default -> {
                    message = "An unexpected error occurred while changing password.";
                    yield HttpStatus.INTERNAL_SERVER_ERROR;
                }
            };
            return new ResponseEntity<>(new StandardResponse<>(message, errorCode, null), status);
        }
    }
}