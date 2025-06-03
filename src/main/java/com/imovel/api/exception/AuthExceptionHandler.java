package com.imovel.api.exception;

import com.imovel.api.error.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class AuthExceptionHandler {
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorCode> handleAuthenticationException(AuthenticationException ex) {
        ErrorCode error = new ErrorCode("AUTHENTICATION_FAILED", ex.getMessage(),HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ErrorCode> handleTokenRefreshException(TokenRefreshException ex) {
        ErrorCode error = new ErrorCode("TOKEN_REFRESH_FAILED", ex.getMessage(),HttpStatus.FORBIDDEN);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }
}