package com.imovel.api.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends ApplicationException {
    public BusinessRuleException(long code, String message) {
        super(code, message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}