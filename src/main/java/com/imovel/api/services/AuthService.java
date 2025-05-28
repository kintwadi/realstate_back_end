package com.imovel.api.services;

import com.imovel.api.model.User;
import com.imovel.api.model.enums.UserRole;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.request.ChangePasswordRequestDto;
import com.imovel.api.request.UserRegistrationRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;

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
        newUser.setPassword(request.getPassword());
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
            if (password.equals(user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Transactional
    public String changeUserPassword(ChangePasswordRequestDto changePasswordRequestDto) {
        Optional<User> userOptional = userRepository.findByEmail(changePasswordRequestDto.getUserEmail());
        if (userOptional.isEmpty()) {
            return "USER_NOT_FOUND";
        }

        User user = userOptional.get();

        if (user.getPassword().equals(changePasswordRequestDto.getOldPassword())) {
            user.setPassword(changePasswordRequestDto.getNewPassword());
            System.out.println("WARNING: Setting plain text password for user " + user.getEmail());
            userRepository.save(user);
            return null; // Success
        } else {
            return "OLD_PASSWORD_MISMATCH";
        }
    }
}