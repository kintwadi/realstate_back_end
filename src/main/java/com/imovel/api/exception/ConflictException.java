package com.imovel.api.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ApiException {
    public ConflictException(long code, String message) {
        super(code, message, HttpStatus.CONFLICT);
    }
}
