package com.imovel.api.config;

import com.imovel.api.security.token.JWTProvider;
import com.imovel.api.security.token.Token;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.*;

@TestConfiguration
public class TestJwtConfig {

    @Bean
    @Primary
    public JWTProvider mockJwtProvider() {
        JWTProvider mockProvider = mock(JWTProvider.class);
        
        // Mock the initialize method to do nothing
        doNothing().when(mockProvider).initialize();
        
        // Mock token generation
        Token mockToken = new Token("mock-access-token", "mock-refresh-token");
        when(mockProvider.generateToken()).thenReturn(mockToken);
        
        // Mock token validation
        when(mockProvider.validateAccessToken(anyString())).thenReturn(true);
        when(mockProvider.validateRefreshToken(anyString())).thenReturn(true);
        
        // Mock claim operations
        doNothing().when(mockProvider).addClaim(anyString(), anyString());
        
        return mockProvider;
    }
}