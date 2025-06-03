package com.imovel.api.exception;

import com.imovel.api.error.ApiCode;
import com.imovel.api.error.ErrorCode;
import com.imovel.api.response.StandardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<StandardResponse<?>> handleApplicationException(ApplicationException ex) {
        return ResponseEntity.status(ex.getErrorResponse().getStatus())
                .body(StandardResponse.error(ex.getErrorResponse()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<?>> handleGenericException(Exception ex) {
        ErrorCode errorResponse = new ErrorCode(
                ApiCode.SYSTEM_ERROR.getMessage(),
                "An unexpected error occurred",HttpStatus.BAD_REQUEST
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(StandardResponse.error(errorResponse));
    }
}