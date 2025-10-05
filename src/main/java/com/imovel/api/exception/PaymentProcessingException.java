package com.imovel.api.exception;

import org.springframework.http.HttpStatus;

public class PaymentProcessingException extends ApiException {
    
    public PaymentProcessingException(String message) {
        super(5000L, message, HttpStatus.BAD_GATEWAY);
    }
    
    public PaymentProcessingException(long code, String message, HttpStatus httpStatus) {
        super(code, message, httpStatus);
    }
}
