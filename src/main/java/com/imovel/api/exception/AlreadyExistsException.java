package com.imovel.api.exception;

import org.springframework.http.HttpStatus;

public class AlreadyExistsException extends ApiException {
    public AlreadyExistsException(long code, String message) {
        super(code, message, HttpStatus.CONFLICT);
    }
}
