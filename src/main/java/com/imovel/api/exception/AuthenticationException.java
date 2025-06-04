package com.imovel.api.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends ApplicationException {
    public AuthenticationException(long code, String message) {
        super(code, message, HttpStatus.UNAUTHORIZED);
    }
}