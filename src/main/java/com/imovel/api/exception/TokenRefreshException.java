package com.imovel.api.exception;

import org.springframework.http.HttpStatus;

public class TokenRefreshException extends ApplicationException {
    public TokenRefreshException(String code, String message, HttpStatus httpStatus) {
        super(code, message, httpStatus);
    }
}