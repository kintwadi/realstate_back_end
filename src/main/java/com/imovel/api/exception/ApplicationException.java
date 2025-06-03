package com.imovel.api.exception;

import com.imovel.api.error.ErrorCode;
import org.springframework.http.HttpStatus;

public abstract class ApplicationException extends RuntimeException {
    private final ErrorCode errorResponse;

    public ApplicationException(String code, String message, HttpStatus httpStatus) {
        super(message);
        this.errorResponse = new ErrorCode(code, message,httpStatus);
    }

    public ErrorCode getErrorResponse() {
        return errorResponse;
    }

}