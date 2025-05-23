package com.imovel.api.services;

import com.imovel.api.model.User;
import com.imovel.api.model.UserRole;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.request.UserRegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> registerUser(UserRegistrationRequest request) {
        // Check if user with the given email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return Optional.empty(); // User already exists
        }

        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword())); // Hash the password
        newUser.setPhone(request.getPhone());
        newUser.setRole(UserRole.CLIENT);

        return Optional.of(userRepository.save(newUser));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> loginUser(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Compare the provided password with the stored hashed password
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user); // Login successful
            }
        }
        return Optional.empty(); // User not found or password does not match
    }
}