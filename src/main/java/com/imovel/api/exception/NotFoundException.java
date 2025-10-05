package com.imovel.api.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiException {
    public NotFoundException(long code, String message) {
        super(code, message, HttpStatus.NOT_FOUND);
    }
}
