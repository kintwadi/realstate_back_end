package com.imovel.api.services;

import com.imovel.api.model.User;
import com.imovel.api.model.UserRole;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.request.UserRegistrationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> registerUser(UserRegistrationRequest request) {
        // Check if user with the given email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return Optional.empty(); // User already exists
        }

        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(request.getPassword()); // Hash the password
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
            if (password.equals(user.getPassword())) {
                return Optional.of(user); // Login successful
            }
        }
        return Optional.empty(); // User not found or password does not match
    }
}