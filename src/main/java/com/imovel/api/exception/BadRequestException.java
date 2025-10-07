package com.imovel.api.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiException {
    public BadRequestException(long code, String message) {
        super(code, message, HttpStatus.BAD_REQUEST);
    }
}
