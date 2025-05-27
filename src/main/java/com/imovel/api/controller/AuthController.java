package com.imovel.api.controller;

import com.imovel.api.request.UserLoginRequest;
import com.imovel.api.request.UserRegistrationRequest;
import com.imovel.api.response.StandardResponse;
import com.imovel.api.services.UserService;
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

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register") //
    public ResponseEntity<StandardResponse<Object>> register(@RequestBody UserRegistrationRequest request) {

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

    @PostMapping("/reset-password")
    public StandardResponse<Void> resetPassword() {
        // TODO: Implement reset password logic
        return new StandardResponse<>();
    }
}