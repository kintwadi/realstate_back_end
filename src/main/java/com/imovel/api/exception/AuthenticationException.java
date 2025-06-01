package com.imovel.api.exception;

// Custom exceptions
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}

