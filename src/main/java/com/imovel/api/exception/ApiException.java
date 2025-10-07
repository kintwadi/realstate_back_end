package com.imovel.api.exception;

import com.imovel.api.error.ErrorCode;
import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {
    private final ErrorCode errorCode;

    public ApiException(long code, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = new ErrorCode(code, message,httpStatus);
    }
    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
