package com.imovel.api.exception;

import org.springframework.http.HttpStatus;
import java.util.List;

public class ValidationException extends ApplicationException {
    private final List<String> details;

    public ValidationException(String code, String message,HttpStatus httpStatus, List<String> details) {
        super(code, message, httpStatus);
        this.details = details;
    }

    public List<String> getDetails() {
        return details;
    }
}