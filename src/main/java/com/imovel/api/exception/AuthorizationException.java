package com.imovel.api.exception;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends ApplicationException {
    public AuthorizationException(long code, String message, HttpStatus httpStatus) {
        super(code, message,httpStatus);
    }
}