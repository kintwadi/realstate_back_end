package com.imovel.api.exception;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends ApiException {
    public AuthorizationException(long code, String message, HttpStatus httpStatus) {
        super(code, message,httpStatus);
    }
}