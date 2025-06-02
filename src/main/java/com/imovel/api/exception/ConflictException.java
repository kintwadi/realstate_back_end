package com.imovel.api.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApplicationException {
    public ConflictException(String code, String message) {
        super(code, message, HttpStatus.CONFLICT);
    }
}