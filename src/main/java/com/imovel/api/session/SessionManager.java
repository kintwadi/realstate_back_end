package com.imovel.api.session;

import com.imovel.api.model.User;
import com.imovel.api.repository.UserRepository;
import com.imovel.api.services.TokenService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;


@Service
public class SessionManager {
    
    private final TokenService tokenService;
    private final  UserRepository userRepository;
    
    // Constructor injection (recommended)
    public SessionManager(TokenService tokenService,UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }
    
    public CurrentUser getCurrentUser(HttpSession session) {
        String token = (String) session.getAttribute("token");
        
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("No token found in session");
        }
        
        String id = tokenService.getClaim("userId", token);
        String userName = tokenService.getClaim("username", token);
        String role = tokenService.getClaim("role", token);
        
        if (id == null || userName == null) {
            throw new IllegalStateException("Invalid token: missing required claims");
        }
        
        return new CurrentUser(Long.parseLong(id), userName,role);
    }

    public User getCurrentAuthenticatedUser(HttpSession session) {

        return userRepository.findById(getCurrentUser(session).getUserId()).get();
    }
}