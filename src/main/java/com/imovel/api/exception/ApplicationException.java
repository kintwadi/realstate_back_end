package com.imovel.api.exception;

import com.imovel.api.response.ErrorResponse;
import org.springframework.http.HttpStatus;

public abstract class ApplicationException extends RuntimeException {
    private final ErrorResponse errorResponse;
    private final HttpStatus httpStatus;

    public ApplicationException(String code, String message, HttpStatus httpStatus) {
        super(message);
        this.errorResponse = new ErrorResponse(code, message);
        this.httpStatus = httpStatus;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}